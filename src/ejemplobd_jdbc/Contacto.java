package ejemplobd_jdbc;

import org.w3c.dom.*;

public class Contacto {

    private int idContacto;
    private String nombre;
    private String apellidos;
    private String telefono;
    private String correo;
    
    public static final int TAM_NOMBRE = 50;
    public static final int TAM_APELLIDOS = 100;
    public static final int TAM_TELEFONO = 15;
    public static final int TAM_CORREO = 50;
        
    protected int getIdContacto() {
        return idContacto;
    }

    protected String getNombre() {
        return nombre;
    }

    protected void setNombre(String nombre) {
        this.nombre = nombre;
    }

    protected String getApellidos() {
        return apellidos;
    }

    protected void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    protected String getTelefono() {
        return telefono;
    }

    protected void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    protected String getCorreo() {
        return correo;
    }

    protected void setCorreo(String correo) {
        this.correo = correo;
    }
    
    protected Contacto() {
        this.nombre = "";
        this.apellidos = "";
        this.telefono = "";
        this.correo = "";
    }

    protected Contacto(int idContacto, String nombre, String apellidos, String telefono, String correo) {
        this.idContacto = idContacto;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.correo = correo;
    }
    
    protected Contacto(Element elementoContacto) {
        NodeList listaNombres = elementoContacto.getElementsByTagName("NOMBRE");
        this.nombre = listaNombres.item(0).getTextContent();
        NodeList listaApellidos = elementoContacto.getElementsByTagName("APELLIDOS");
        this.apellidos = listaApellidos.item(0).getTextContent();
        NodeList listaTelefono = elementoContacto.getElementsByTagName("TELEFONO");
        this.telefono = listaTelefono.item(0).getTextContent();
        NodeList listaCorreo = elementoContacto.getElementsByTagName("CORREO");
        this.correo = listaCorreo.item(0).getTextContent();
    }

    protected Element toElement(Document documento) {
        Element elementoContacto = documento.createElement("CONTACTO");

        Element elementoNombre = documento.createElement("NOMBRE");
        elementoContacto.appendChild(elementoNombre);
        Text textoNombre = documento.createTextNode(this.nombre);
        elementoNombre.appendChild(textoNombre);

        Element elementoApellidos = documento.createElement("APELLIDOS");
        elementoContacto.appendChild(elementoApellidos);
        Text textoApellidos = documento.createTextNode(this.apellidos);
        elementoApellidos.appendChild(textoApellidos);

        Element elementoTelefono = documento.createElement("TELEFONO");
        elementoContacto.appendChild(elementoTelefono);
        Text textoTelefono = documento.createTextNode(this.telefono);
        elementoTelefono.appendChild(textoTelefono);
        
        Element elementoCorreo = documento.createElement("CORREO");
        elementoContacto.appendChild(elementoCorreo);
        Text textoCorreo = documento.createTextNode(this.correo);
        elementoCorreo.appendChild(textoCorreo);
        
        return elementoContacto;        
    }

}
