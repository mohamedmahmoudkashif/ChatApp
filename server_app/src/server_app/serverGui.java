package server_app;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;

public class serverGui {
	ServerSocket sv;
	ServerSocket gsv;
	serverMain servermain;
	serverMain groupservermain;
	private JFrame frame;
	JButton btnNewButton_1;
	JTextArea txtrServerLogs;
	ArrayList<DataOutputStream> doses = new ArrayList<>();
	ArrayList<String> usernames = new ArrayList<>();
	ArrayList<String> groups = new ArrayList<>();
	HashMap<String,ArrayList<DataOutputStream>> dosesofgroups = new HashMap<String,ArrayList<DataOutputStream>>();
	JPanel list_panel;
	DefaultListModel model = new DefaultListModel();
	JList list;
	//DefaultListModel<String> model = new DefaultListModel<>();
	public class serverMain extends Thread{
		ServerSocket sv;
		public JTextArea jx ;
		public serverMain(ServerSocket sv) {
	        this.sv = sv;
	    }

		
		
		public void run() {
			try {
	            //1.Create Server Socket
					txtrServerLogs.append("\n waiting for clients");
					while (true) {
	            	
	                //2.Listen for Clients
	                Socket c;
	                c = sv.accept();	             
	                //txtrServerLogs.append("\n New Client Arrived");
	                clientListener ch = new clientListener(c);
	                ch.start();

	            }

	        } catch (Exception e1) {
	            System.out.println(e1.getMessage());
	        }
		}
		
		

	}
	public class clientListener extends Thread{
		 private Socket client;
         private String userlistener;
		    // constructor
		    public clientListener(Socket client) {
		        this.client = client;
		    }

		    public void run() {
		        try {
		        	DataOutputStream dos = new DataOutputStream(client.getOutputStream());
		            DataInputStream dis = new DataInputStream(client.getInputStream());
		            
		            
		            while (true) {
		                String AN = dis.readUTF();
		                System.out.println("Listening to :" +userlistener+" Message is : "+AN );
		                System.out.println("");
		                
		                if(AN.contains("newClient"))
		                {
		                	doses.add(dos);
		                	String []user = AN.split(":");
		                	//System.out.println(user[1]);
		                	usernames.add(user[1]);
		                	userlistener = user[1];
		                	//System.out.println("created a new user :" + userlistener);
		                	model.addElement(user[1]);
		                	list.setModel(model);
		                	txtrServerLogs.append("\n new user added:"+user[1]);
		                	dos.writeUTF("connection successfull!");
		                	String active = "";
		                	for(String userx:usernames)
		                	{
		                		if(!userx.equals(user[1]))
		                		active = active+","+userx;
		                	}
		                	if(!active.equals(""))
		                	{
		                		dos.writeUTF("activeUsers"+active);
		                		System.out.println(active);
		                	}
		                	for(DataOutputStream data :doses)
		                	{
		                		if(data != dos)
		                		{
		                			data.writeUTF("updateUsers:"+user[1]);
		                		}
		                	}
		                	
		                	
		                }
		                else if(AN.contains("$From"+userlistener+"$"))
		                {
		                System.out.println(AN);
		                if(AN.contains("CreateGroup")){
		                	String []order = AN.split(":");
		                	//System.out.println(AN);
		                	
				           
		                	txtrServerLogs.append("\n new group added: name:" +order[5]+ " admin:"+ order[3]);
		                	groups.add(order[5]);
		                	String []sendees = order[1].split(",");
		                	ArrayList<DataOutputStream> dosesofAgroup = new ArrayList<DataOutputStream>();
		                	for(String se : sendees){
		                		for(int i= 0; i < usernames.size() ; i++){
		                			if(se.equals(usernames.get(i))){
		                				dosesofAgroup.add(doses.get(i));
		                				doses.get(i).writeUTF("OpenGroupGui:"+order[5]+"&users&"+order[1]);
		                				doses.get(i).writeUTF("toGroup:"+order[5]);
		                			}
		                		}
		                	}
		                	 dosesofgroups.put(order[5], dosesofAgroup);
		                	 System.out.println("Groups Available:" +dosesofgroups.size());
		                }else if(AN.contains("FromGroup")){
		                	String []groupOb = AN.split(":");
		                	String groupname = groupOb[1];
		                	ArrayList<DataOutputStream> getdoses = new ArrayList<>() ;
		                	getdoses = dosesofgroups.get(groupname);
		                	System.out.println("Sending to "+groupname+" which contains "+getdoses.size());
		                	//for(DataOutputStream data: dosesofgroups.get(groupname))
		                	for (DataOutputStream data : getdoses)
			                {
		                		System.out.println("writing to one of the clients in group"+groupname);
			                	data.writeUTF("toGroup:"+groupname+":"+groupOb[2]);
			                }
			                txtrServerLogs.append("\n"+"Sent Stuff to group:"+groupname);
		                }
		                else
		                {
		                	
			                for (DataOutputStream data : doses)
			                {
			                	data.writeUTF(AN.split(":")[1]);
			                }
			                txtrServerLogs.append("\n"+"Sent Stuff to the clients");
		                }
		                }
		            }
		            //Close/release resources
		            //dis.close();
		           // dos.close();
		            //client.close();
		        } catch (Exception e) {
		            System.out.println(e.getMessage());
		        }
		    }

	}

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					serverGui window = new serverGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}

	/**
	 * Create the application.
	 */
	public serverGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		list_panel = new JPanel();
		list_panel.setBounds(0, 0, 125, 251);
		panel.add(list_panel);
		list_panel.setLayout(null);
		
		list = new JList();
		list.setBounds(10, 35, 105, 205);
		list_panel.add(list);
		
		JLabel lblActiveUsers = new JLabel("Active Users");
		lblActiveUsers.setBounds(10, 11, 105, 14);
		list_panel.add(lblActiveUsers);
		
		JPanel connection_panel = new JPanel();
		connection_panel.setBounds(127, 0, 297, 38);
		panel.add(connection_panel);
		
		JButton btnNewButton = new JButton("Connect");
		
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				/*try {
		            //1.Create Server Socket
		           // sv = new ServerSocket(1234);
		            btnNewButton.setEnabled(false);
		            while (true) {
		                //2.Listen for Clients
		                Socket c;
		                c = sv.accept();
		                System.out.println("New Client Arrived");
		                clientListener ch = new clientListener(c);
		                ch.start();

		            }

		        } catch (Exception e1) {
		            System.out.println(e1.getMessage());
		        }*/
				btnNewButton.setEnabled(false);
				
				try {
					//Client Server Socket 	
					sv = new ServerSocket(1234);
					servermain = new serverMain(sv);
					servermain.start();
					//txtrServerLogs.setText(txtrServerLogs.getText()+"\n"+servermain.message);
					btnNewButton_1.setEnabled(true);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
			}
		});
		connection_panel.add(btnNewButton);
		
		btnNewButton_1 = new JButton("Disconnect");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					sv.close();
					btnNewButton.setEnabled(true);
					btnNewButton_1.setEnabled(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
		connection_panel.add(btnNewButton_1);
		btnNewButton_1.setEnabled(false);
		JPanel log = new JPanel();
		log.setBounds(127, 39, 297, 186);
		panel.add(log);
		log.setLayout(null);
		
		txtrServerLogs = new JTextArea();
		txtrServerLogs.setEditable(false);
		txtrServerLogs.setBounds(43, 0, 222, 186);
		txtrServerLogs.setText("Server Logs");
		log.add(txtrServerLogs);
		
		JPanel contol_users_panel = new JPanel();
		contol_users_panel.setBounds(126, 226, 298, 36);
		panel.add(contol_users_panel);
		
		JButton btnDeleteUser = new JButton("Delete User");
		contol_users_panel.add(btnDeleteUser);
	}
}
