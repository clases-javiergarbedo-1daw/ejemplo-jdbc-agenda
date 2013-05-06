package ejemplobd_jdbc;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;
import java.awt.Frame;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.derby.drda.NetworkServerControl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BDAgenda {

    static Connection conexion;

    static final int ORDEN_RECIENTES = 0;
    static final int ORDEN_ANTIGUOS = 1;
    static final int ORDEN_NOMBRE = 2;
    static final int ORDEN_APELLIDOS = 3;
    
    static String filtro = "";

    /**
     * Establece la conexión con la base de datos siguiendo la configuración que
     * se haya establecido en la aplicación.
     */
    protected static void conectarBDAgenda() {
        String protocolo = "";
        String servidor = "";
        String BD = "";
        String usuario = "";
        String password = "";
    
        Properties propiedades = new Properties();
        
        try {
            //Usar un fichero de propiedades para guardar los datos
            //  de conexión con el servidor MySQL
            FileReader fichProp = new FileReader("agenda.cfg");
            propiedades.load(fichProp);  
            protocolo = propiedades.getProperty("protocolo");
            servidor = propiedades.getProperty("servidor");
            BD = propiedades.getProperty("BD");
            usuario = propiedades.getProperty("usuario");
            password = propiedades.getProperty("password");
        } catch(Exception e) {
            /* Si no hay fichero de propiedades se toma por defecto
             * la base de datos interna. No hay que hacer nada
             */
        }
        
        //Conectar con la base de datos en función del tipo de servidor
        //  que se haya seleccionado en la configuración de la aplicación
        try {
            //Según la información obtenida del fichero de propiedades
            //  se sabe si la conexión debe ser a MySQL o no
            if(protocolo.equals("jdbc:mysql://")) {        
                //Conexión con el servidor MySQL
                conexion = DriverManager.getConnection(
                        protocolo + servidor + "/" + BD,
                        usuario, password);
                //Si se detecta que no existen las tablas, se crean
                if(!existenTablas()) {
                    crearTablasMySQL();
                }
            } else { //Conexión con el servidor Derby local         
                //Arrancar el servidor Derby
                //Requiere las librerías derby.jar y derbynet.jar
                NetworkServerControl serverControl=new NetworkServerControl();
                serverControl.start(new PrintWriter(System.out,true));

                //Conectar con la BD de Derby, creando la BD si es necesario
                //Requiere la librería derbyclient.jar
                Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
                conexion = DriverManager.getConnection("jdbc:derby://"
                        + "localhost/agenda;create=true;"
                        + "user=usuarioAgenda;password=passAgenda"); 
                //Si se detecta que no existen las tablas, se crean
                if(!existenTablas()) {
                    crearTablasDerby();
                }
            }
        } catch(Exception e) {
            //No se ha podido realizar la conexión con la bae de datos
            //Se pide al usuario si quiere cambiar la configuración de conexión
            int respuesta = JOptionPane.showConfirmDialog(null,
                    "No se ha podido conectar con la base de datos\n"
                    + "¿Desea cambiar la configuración de conexión?",
                    "Atención", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if(respuesta==JOptionPane.YES_OPTION) {
                DialogoConfiguracion dialogConfig = new DialogoConfiguracion(Frame.getFrames()[0], true);
                dialogConfig.setVisible(true);
                dialogConfig.dispose();
                //Volver a intentar la conexión
                BDAgenda.conectarBDAgenda();
            } else {
                //Si el usuario no cambia la configuración de conexión se cierra la aplicación
                System.exit(0);
            }
        }                                
    }
            
    
    private static boolean existenTablas() {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            String textoSQL = "SELECT * FROM contactos ";
            ResultSet resultados = sentenciaSQL.executeQuery(textoSQL);
            resultados.next();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private static void crearTablasDerby() {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("CREATE TABLE contactos ("
                + "idcontacto INTEGER NOT NULL GENERATED BY DEFAULT AS IDENTITY, "
                + "nombre varchar(50) DEFAULT NULL, "
                + "apellidos varchar(100) DEFAULT NULL, "
                + "telefono varchar(15) DEFAULT NULL, "
                + "correo varchar(20) DEFAULT NULL, "
                + "PRIMARY KEY (idcontacto) )");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido las tablas necesarias en la base de datos\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    private static void crearTablasMySQL() {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("CREATE TABLE IF NOT EXISTS contactos ("
                + "idcontacto int(11) NOT NULL AUTO_INCREMENT, "
                + "nombre varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL, "
                + "apellidos varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL, "
                + "telefono varchar(15) COLLATE utf8_unicode_ci DEFAULT NULL, "
                + "correo varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL, "
                + "PRIMARY KEY (idcontacto) "
                + ") ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido las tablas necesarias en la base de datos\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Permite obtener todos los contactos que se encuentran en la BD siguiendo
     * el orden que se indique como parámetro.
     * 
     * @param orden Indicador del tipo de orden que se quiere establacer para 
     * obtener los datos. Para establecer el orden se usarán las constantes 
     * predefinidas en esta clase.
     * 
     * @return Lista de todos los contactos obtenidos de la BD.
     */
    protected static ArrayList<Contacto> leerContactos(int orden) {
        ArrayList<Contacto> listaContactos = new ArrayList();
        ResultSet resultados = null;
        try {
            Statement sentenciaSQL = conexion.createStatement();
            String textoSQL = "SELECT * FROM contactos ";
            if(!filtro.isEmpty()) {
                textoSQL += "WHERE nombre LIKE '%"+filtro+"%' OR "
                        + "apellidos LIKE '%"+filtro+"%' OR "
                        + "apellidos LIKE '%"+filtro+"%' OR "
                        + "telefono LIKE '%"+filtro+"%' OR "
                        + "correo LIKE '%"+filtro+"%' ";
            }
            switch (orden) {
                case ORDEN_RECIENTES:
                    textoSQL += "ORDER BY idcontacto DESC ";
                    break;
                case ORDEN_ANTIGUOS:
                    textoSQL += "ORDER BY idcontacto ";
                    break;
                case ORDEN_NOMBRE:
                    textoSQL += "ORDER BY nombre ";
                    break;
                case ORDEN_APELLIDOS:
                    textoSQL += "ORDER BY APELLIDOS ";
                    break;
            }
            resultados = sentenciaSQL.executeQuery(textoSQL);
            while (resultados.next()) {
                int idContacto = resultados.getInt("idContacto");
                String nombre = resultados.getString("nombre");
                String apellidos = resultados.getString("apellidos");
                String telefono = resultados.getString("telefono");
                String correo = resultados.getString("correo");
                //Crear un objeto Contacto con los datos obtenidos
                Contacto contacto = new Contacto(
                        idContacto, nombre, apellidos, telefono, correo);
                //Guardar el contacto en la lista que se retornará
                listaContactos.add(contacto);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido leer los datos de contactos\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
        return listaContactos;
    }

    /**
     * Obtiene el contacto cuyo identificador coincide con el indicado por parámetro.
     * 
     * @param idContacto Identificador del contacto del que se quieren obtener los datos.
     * 
     * @return Objeto de tipo Contacto con todos los datos del contacto cuyo
     * identificador coincide con el indicado por parámetro. En caso de que no 
     * se encuentre ningún contacto con el identificador indicado retorna null.
     */
    protected static Contacto leerContacto(int idContacto) {
        Contacto contacto = null;
        try {
            Statement sentenciaSQL = conexion.createStatement();
            ResultSet resultados = sentenciaSQL.executeQuery(
                    "SELECT * FROM contactos WHERE idContacto = " + idContacto);
            if (resultados.next()) {
                String nombre = resultados.getString("nombre");
                String apellidos = resultados.getString("apellidos");
                String telefono = resultados.getString("telefono");
                String correo = resultados.getString("correo");
                //Crear un objeto Contacto con los datos obtenidos
                contacto = new Contacto(
                        idContacto, nombre, apellidos, telefono, correo);
            } else {
                JOptionPane.showMessageDialog(null,
                        "No se ha encontrado el contacto en la base de datos",
                        "Error", JOptionPane.ERROR_MESSAGE);;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido leer los datos del contacto\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
        return contacto;
    }

    /** 
     * Permite actualizar en la base de datos el contacto que se pase por 
     * parámetro, sustituyendo los datos del contacto cuyo identificador 
     * coincide con el del contacto pasado por parámetro.
     * 
     * @param contacto Objeto Contacto que contiene los datos que se van a
     * actualizar en la base de datos.
     */
    protected static void guardarContacto(Contacto contacto) {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("UPDATE contactos SET "
                    + "nombre = '" + contacto.getNombre() + "', "
                    + "apellidos = '" + contacto.getApellidos() + "', "
                    + "telefono = '" + contacto.getTelefono() + "', "
                    + "correo = '" + contacto.getCorreo() + "' "
                    + "WHERE idContacto = " + contacto.getIdContacto());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido actualizar los datos del contacto\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Añade un nuevo contacto a la base de datos
     * 
     * @param contacto Objeto Contacto que contiene los datos que se van a
     * insertar en la base de datos.
     */
    protected static void añadirContacto(Contacto contacto) {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("INSERT INTO contactos "
                    + "(nombre, apellidos, telefono, correo) VALUES ("
                    + "'" + contacto.getNombre() + "', "
                    + "'" + contacto.getApellidos() + "', "
                    + "'" + contacto.getTelefono() + "', "
                    + "'" + contacto.getCorreo() + "')");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se han podido añadir los datos del nuevo contacto\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Suprime de la base de datos el contacto cuyo identificador coindice conç
     * el del contacto pasado por parámetro
     * 
     * @param contacto Objeto Contacto que contiene los datos del contacto que
     * se desea eliminar
     */
    protected static void suprimirContacto(Contacto contacto) {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("DELETE FROM contactos "
                    + "WHERE idContacto = " + contacto.getIdContacto());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido eliminar el contacto\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Elimina todo el contenido de la base de datos
     */
    protected static void borrarBaseDatos() {
        try {
            Statement sentenciaSQL = conexion.createStatement();
            sentenciaSQL.executeUpdate("DELETE FROM contactos");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido borrar la base de datos\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Realiza una copia de seguridad de toda la base de datos, guardando su
     * contenido en un fichero XML.
     * 
     * @param fichero Referencia al fichero en el que se va a guardar la copia
     * de seguridad de la base de datos en formato XML.
     */
    protected static void copiaSeguridad(File fichero) {
        try {
            DocumentBuilderFactory fábricaCreadorDocumento = DocumentBuilderFactory.newInstance();
            DocumentBuilder creadorDocumento = fábricaCreadorDocumento.newDocumentBuilder();
            Document documento = creadorDocumento.newDocument();
            
            Element elementoRaiz = documento.createElement("AGENDA");
            documento.appendChild(elementoRaiz);
            
            ResultSet resultados = null;
            Statement sentenciaSQL = conexion.createStatement();
            String textoSQL = "SELECT * FROM contactos ";
            resultados = sentenciaSQL.executeQuery(textoSQL);
            while (resultados.next()) {
                int idContacto = resultados.getInt("idContacto");
                String nombre = resultados.getString("nombre");
                String apellidos = resultados.getString("apellidos");
                String telefono = resultados.getString("telefono");
                String correo = resultados.getString("correo");
                //Crear un objeto Contacto con los datos obtenidos
                Contacto contacto = new Contacto(
                        idContacto, nombre, apellidos, telefono, correo);
                //Colgar el contacto en el documento XML
                Element elementoContacto = contacto.toElement(documento);
                elementoRaiz.appendChild(elementoContacto);
            }          
            //Volcar el documento XML al fichero
            TransformerFactory fábricaTransformador = TransformerFactory.newInstance();
            Transformer transformador = fábricaTransformador.newTransformer();
            transformador.setOutputProperty(OutputKeys.INDENT, "yes");
            transformador.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "3");
            Source origen = new DOMSource(documento);
            Result destino = new StreamResult(fichero);
            transformador.transform(origen, destino);            
            JOptionPane.showMessageDialog(null,
                    "Copia de seguridad finalizada\n",                    
                    "Copia de seguridad", JOptionPane.INFORMATION_MESSAGE);;
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido realizar la copia de seguridad\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
    
    /**
     * Recupera todos los datos almacenados en un fichero de copia de seguridad
     * que se encuentra en formato XML.
     * 
     * @param fichero Referencia al fichero en el que se encuentra la copia
     * de seguridad de la base de datos en formato XML.
     */
    protected static void restaurarCopia(File fichero) {
        try {
            DocumentBuilderFactory fábricaCreadorDocumento = DocumentBuilderFactory.newInstance();
            DocumentBuilder creadorDocumento = fábricaCreadorDocumento.newDocumentBuilder();
            Document documento = creadorDocumento.parse(fichero);
            
            Element elementoRaiz = documento.getDocumentElement();
            //Cargar los datos de los contactos
            NodeList listaContactos = documento.getElementsByTagName("CONTACTO");
            for(int i=0; i<listaContactos.getLength(); i++) {
                Element elementoContacto = (Element)listaContactos.item(i);
                Contacto contacto = new Contacto(elementoContacto);
                añadirContacto(contacto);
            }
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null,
                    "No se ha podido restaurar la copia de seguridad\n"
                    + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);;
            e.printStackTrace();
        }
    }
}
