package ejemplobd_jdbc;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class ContactoListaPanel extends javax.swing.JPanel {

    private DefaultTableModel modeloTabla;
    private int ordenContactos = BDAgenda.ORDEN_APELLIDOS;

    public void setOrdenContactos(int ordenContactos) {
        this.ordenContactos = ordenContactos;
    }
        
    /**
     * Creates new form ContactoListaPanel
     */
    public ContactoListaPanel() {
        initComponents();
        //Impedir que se pueda mover la barra de herramientas
        jToolBar1.setFloatable(false);     
        //Dejar en blanco el campo de búsqueda
        jTextFieldBuscar.setText("");
    }

    void CargarDatosJTable() {
        ArrayList<Contacto> listaContactos = new ArrayList();
         
        modeloTabla = new DefaultTableModel() {
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return false;
            }                
        };
        jTable1.setModel(modeloTabla);

        //Rellenar las cabeceras de las columnas
        String[] cabecera = {"IdContacto", "Nombre", "Apellidos"};
        modeloTabla.setColumnIdentifiers(cabecera);

        listaContactos = BDAgenda.leerContactos(this.ordenContactos);
        //Recorrer la lista de contactos para añadir algunos datos en el JTable
        for (Contacto contacto :listaContactos) {
            //Se va mostrar sólo el nombre y los apellidos
            String[] datosFilaContacto = {
                String.valueOf(contacto.getIdContacto()), 
                contacto.getNombre(), 
                contacto.getApellidos()};
            modeloTabla.addRow(datosFilaContacto);
        }        

        //Establecer que sólo se pueda seleccionar una fila
        jTable1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        //Ocultar columna de idContacto
        TableColumn tc = jTable1.getColumn("IdContacto");
        jTable1.removeColumn(tc);
        
        //Mostrar vacíos los datos de contacto
        mostrarDatosContacto();        
    }
    
    void nuevo() {
        ContactoDialogo dialogoContacto = new ContactoDialogo(Frame.getFrames()[0], true);
        //Crear un nuevo objeto contacto
        Contacto contacto = new Contacto();
        //Asignar el contacto obtenido a la ventana de diálogo
        dialogoContacto.setContacto(contacto);
        //Mostar la ventana con los campos de edición activados
        dialogoContacto.activarCampos(true);
        dialogoContacto.setVisible(true);
        //Liberar la memoria de pantalla ocupada por la ventana de detalle
        dialogoContacto.dispose();
        //Comprobar si se ha pulsado Aceptar o Cancelar 
        if(dialogoContacto.isAceptado()) {
            //Añadir el contacto en la BD
            BDAgenda.añadirContacto(contacto);        
            //Actualiza los datos en la tabla de la ventana
            CargarDatosJTable();
        } 
    }    
    
    void editar() {
        //Obtener número de fila seleccionada en el JTable
        int numFilaSelec = jTable1.getSelectedRow();
        //Comprobar que el usuario ha seleccionado alguna fila
        if(numFilaSelec!=-1) {
            ContactoDialogo dialogoContacto = new ContactoDialogo(Frame.getFrames()[0], true);
            //Obtener el contacto correspondiente a la fila seleccionada
            Contacto contacto = BDAgenda.leerContacto(
                    Integer.valueOf((String)modeloTabla.getValueAt(numFilaSelec, 0)));
            //Asignar el contacto obtenido a la ventana de diálogo
            dialogoContacto.setContacto(contacto);
            //Mostar la ventana con los detalles del contacto desactivados
            dialogoContacto.activarCampos(false);
            dialogoContacto.setVisible(true);
            //Liberar la memoria de pantalla ocupada por la ventana de detalle
            dialogoContacto.dispose();
            //Comprobar si se ha pulsado Aceptar o Cancelar 
            if(dialogoContacto.isAceptado()) {
                //Guardar el contacto en la BD
                BDAgenda.guardarContacto(contacto);   
                //Actualiza los datos en la tabla de la ventana
                CargarDatosJTable();
            } 
        } else {
            //Si no se ha seleccionado un contacto de la lista hay que notificarlo
            JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar un contacto previamente", 
                    "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }

    void suprimir() {
        //Obtener número de fila seleccionada en el JTable
        int numFilaSelec = jTable1.getSelectedRow();
        //Comprobar que el usuario ha seleccionado alguna fila
        if(numFilaSelec!=-1) {
            //Obtener el contacto correspondiente a la fila seleccionada
            Contacto contacto = BDAgenda.leerContacto(
                    Integer.valueOf((String)modeloTabla.getValueAt(numFilaSelec, 0)));
            int respuesta = JOptionPane.showConfirmDialog(this, 
                    "¿Desea suprimir el contacto?\n"
                    + contacto.getNombre() + " " + contacto.getApellidos(),
                    "Confirmación",
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
            //Comprobar si se ha pulsado Aceptar o Cancelar 
            if(respuesta==JOptionPane.YES_OPTION) {
                //Suprimir el contacto de la BD
                BDAgenda.suprimirContacto(contacto);   
                //Actualiza los datos en la tabla de la ventana
                CargarDatosJTable();
                //Mostrar vacíos los datos de contacto
                mostrarDatosContacto();
            } 
        } else {
            //Si no se ha seleccionado un contacto de la lista hay que notificarlo
            JOptionPane.showMessageDialog(this, 
                    "Debe seleccionar un contacto previamente", 
                    "Atención", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    void mostrarDatosContacto() {
        //Obtener número de fila seleccionada en el JTable
        int numFilaSelec = jTable1.getSelectedRow();
        //Comprobar que el usuario ha seleccionado alguna fila
        if(numFilaSelec!=-1) {
            //Obtener el contacto correspondiente a la fila seleccionada
            Contacto contacto = BDAgenda.leerContacto(
                    Integer.valueOf((String)modeloTabla.getValueAt(numFilaSelec, 0)));
            jLabelNombre.setText(contacto.getNombre());
            jLabelApellidos.setText(contacto.getApellidos());
            jLabelTelefono.setText(contacto.getTelefono());
            jLabelCorreo.setText(contacto.getCorreo());            
        } else {
            jLabelNombre.setText("");
            jLabelApellidos.setText("");
            jLabelTelefono.setText("");
            jLabelCorreo.setText("");
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItemNuevo = new javax.swing.JMenuItem();
        jMenuItemEditar = new javax.swing.JMenuItem();
        jMenuItemSuprimir = new javax.swing.JMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        jButtonNuevo = new javax.swing.JButton();
        jButtonEditar = new javax.swing.JButton();
        jButtonSuprimir = new javax.swing.JButton();
        jButtonActualizar = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldBuscar = new javax.swing.JTextField();
        jButtonBuscar = new javax.swing.JButton();
        jButtonLimpiar = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabelNombre = new javax.swing.JLabel();
        jLabelApellidos = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabelTelefono = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabelCorreo = new javax.swing.JLabel();

        jMenuItemNuevo.setText("Nuevo...");
        jMenuItemNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemNuevoActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemNuevo);

        jMenuItemEditar.setText("Editar...");
        jMenuItemEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemEditarActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemEditar);

        jMenuItemSuprimir.setText("Suprimir...");
        jMenuItemSuprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSuprimirActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItemSuprimir);

        jToolBar1.setRollover(true);

        jButtonNuevo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/nuevo.png"))); // NOI18N
        jButtonNuevo.setToolTipText("Añadir nuevo contacto");
        jButtonNuevo.setFocusable(false);
        jButtonNuevo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonNuevo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonNuevoActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonNuevo);

        jButtonEditar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/editar.png"))); // NOI18N
        jButtonEditar.setToolTipText("Editar contacto seleccionado");
        jButtonEditar.setFocusable(false);
        jButtonEditar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonEditar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonEditar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonEditarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonEditar);

        jButtonSuprimir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/borrar.png"))); // NOI18N
        jButtonSuprimir.setToolTipText("Suprimir contacto seleccionado");
        jButtonSuprimir.setFocusable(false);
        jButtonSuprimir.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonSuprimir.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonSuprimir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSuprimirActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonSuprimir);

        jButtonActualizar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/actualizar.png"))); // NOI18N
        jButtonActualizar.setToolTipText("Actualizar la lista leyendo de la base de datos");
        jButtonActualizar.setFocusable(false);
        jButtonActualizar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonActualizar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActualizarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonActualizar);
        jToolBar1.add(jSeparator1);

        jLabel6.setText("Buscar:");
        jToolBar1.add(jLabel6);

        jTextFieldBuscar.setColumns(15);
        jTextFieldBuscar.setText("jTextFieldBuscar");
        jTextFieldBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldBuscarKeyTyped(evt);
            }
        });
        jToolBar1.add(jTextFieldBuscar);

        jButtonBuscar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/buscar.png"))); // NOI18N
        jButtonBuscar.setToolTipText("Buscar los contactos coincidentes con el texto");
        jButtonBuscar.setFocusable(false);
        jButtonBuscar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonBuscar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuscarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonBuscar);

        jButtonLimpiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/limpiar.png"))); // NOI18N
        jButtonLimpiar.setToolTipText("Quitar el filtro de búsqueda");
        jButtonLimpiar.setFocusable(false);
        jButtonLimpiar.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButtonLimpiar.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButtonLimpiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLimpiarActionPerformed(evt);
            }
        });
        jToolBar1.add(jButtonLimpiar);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                jTable1MouseReleased(evt);
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jTable1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTable1KeyReleased(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Datos del contacto"));

        jLabel1.setText("Nombre:");

        jLabelNombre.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelNombre.setText("jLabelNombre");

        jLabelApellidos.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelApellidos.setText("jLabelApellidos");

        jLabel3.setText("Apellidos:");

        jLabel4.setText("Teléfono:");

        jLabelTelefono.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelTelefono.setText("jLabelTelefono");

        jLabel5.setText("Correo:");

        jLabelCorreo.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelCorreo.setText("jLabelCorreo");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabelCorreo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelNombre, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelApellidos, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                    .addComponent(jLabelTelefono, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabelNombre))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabelApellidos))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabelTelefono))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabelCorreo))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 287, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 397, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 131, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonNuevoActionPerformed
        nuevo();
    }//GEN-LAST:event_jButtonNuevoActionPerformed

    private void jButtonEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonEditarActionPerformed
        editar();
    }//GEN-LAST:event_jButtonEditarActionPerformed

    private void jButtonSuprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSuprimirActionPerformed
        suprimir();
    }//GEN-LAST:event_jButtonSuprimirActionPerformed

    private void jButtonActualizarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActualizarActionPerformed
        CargarDatosJTable();
    }//GEN-LAST:event_jButtonActualizarActionPerformed

    private void jTextFieldBuscarKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldBuscarKeyTyped
        //Realizar la búsqueda al pulsar la tecla Intro
        if (evt.getKeyChar() == '\n') {
            jButtonBuscar.doClick();
        }
    }//GEN-LAST:event_jTextFieldBuscarKeyTyped

    private void jButtonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuscarActionPerformed
        BDAgenda.filtro = jTextFieldBuscar.getText();
        CargarDatosJTable();
    }//GEN-LAST:event_jButtonBuscarActionPerformed

    private void jButtonLimpiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLimpiarActionPerformed
        jTextFieldBuscar.setText("");
        BDAgenda.filtro = "";
        CargarDatosJTable();
    }//GEN-LAST:event_jButtonLimpiarActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() >= 2) {
            editar();
        } 
        if (evt.getButton() == MouseEvent.BUTTON3) {
            //Seleccionar la fila en la que se hace clic con el botón derecho
            int filaClic = jTable1.rowAtPoint(evt.getPoint());
            jTable1.getSelectionModel().setSelectionInterval(filaClic, filaClic);
            //Mostrar los datos del contacto seleccionado
            mostrarDatosContacto();
            //Mostrar el menú contextual
            jPopupMenu1.show(jTable1, evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jTable1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable1KeyReleased
        mostrarDatosContacto();
    }//GEN-LAST:event_jTable1KeyReleased

    private void jMenuItemNuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemNuevoActionPerformed
        nuevo();
    }//GEN-LAST:event_jMenuItemNuevoActionPerformed

    private void jMenuItemEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemEditarActionPerformed
        editar();
    }//GEN-LAST:event_jMenuItemEditarActionPerformed

    private void jMenuItemSuprimirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSuprimirActionPerformed
        suprimir();
    }//GEN-LAST:event_jMenuItemSuprimirActionPerformed

    private void jTable1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseReleased
        mostrarDatosContacto();
    }//GEN-LAST:event_jTable1MouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonActualizar;
    private javax.swing.JButton jButtonBuscar;
    private javax.swing.JButton jButtonEditar;
    private javax.swing.JButton jButtonLimpiar;
    private javax.swing.JButton jButtonNuevo;
    private javax.swing.JButton jButtonSuprimir;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabelApellidos;
    private javax.swing.JLabel jLabelCorreo;
    private javax.swing.JLabel jLabelNombre;
    private javax.swing.JLabel jLabelTelefono;
    private javax.swing.JMenuItem jMenuItemEditar;
    private javax.swing.JMenuItem jMenuItemNuevo;
    private javax.swing.JMenuItem jMenuItemSuprimir;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextFieldBuscar;
    private javax.swing.JToolBar jToolBar1;
    // End of variables declaration//GEN-END:variables
}
