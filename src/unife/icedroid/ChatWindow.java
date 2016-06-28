package unife.icedroid;

import unife.icedroid.core.ICeDROID;
import unife.icedroid.resources.Constants;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.DefaultListModel;

public class ChatWindow {
    private static final String TAG = "ChatWindow";
	
	public static void open(final Subscription subscription, JList<String> parentList) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				start(subscription, parentList);
			}
			
		});
	}
	
	private static void start(final Subscription subscription, JList<String> parentList) {
		final JFrame window = new JFrame(subscription.toString());
		window.setSize(400, 500);
		window.setResizable(false);
		
		final DefaultListModel<String> listData = loadMessagesForSubscription(subscription);
		JList<String> list = new JList<String>(listData);
		JScrollPane listContainer = new JScrollPane(list);
		listContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		listContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		window.add(listContainer, BorderLayout.CENTER);
	
		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.X_AXIS);
		panel.setLayout(layout);
		final JTextField text = new JTextField(25);
		text.setMaximumSize(new Dimension(1000, 1000));
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String msg = text.getText();
		        if (!msg.equals("")) {
		            text.setText(null);
		            TxtMessage message = new TxtMessage(subscription, msg);
		            try {
			            ChatsManager.getInstance().saveMessageInConversation(message);
		            }
		            catch (IOException ioex) {
		            	System.err.println(TAG + " - " + ioex.getMessage());
		            }
		            ICeDROID.getInstance().send(message);
		        }
			}
			
		});
		panel.add(text);
		panel.add(send);
		window.add(panel, BorderLayout.SOUTH);
		window.getRootPane().setDefaultButton(send);
		
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.setVisible(true);
		
		final SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			
			@Override
			protected Void doInBackground() {
				Path conversationsDir = Paths.get(Constants.CONVERSATIONS_PATH);
				
				try {
					WatchService watcher = conversationsDir.getFileSystem().newWatchService();
					conversationsDir.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
					
					while (true) {
						WatchKey wk = watcher.take();
						for (WatchEvent<?> event : wk.pollEvents()) {
							Path file = (Path) event.context();
							if (file.endsWith(subscription.getSubscriptionFileName())) {
					            try {
					            	String conversationFileName = Constants.CONVERSATIONS_PATH +
					            			subscription.getSubscriptionFileName();
					                BufferedReader br = new BufferedReader(new FileReader(conversationFileName));
					                ArrayList<String> previousMessages = new ArrayList<String>(0);
					                String msg = null;
					                while ((msg = br.readLine()) != null) {
					                    previousMessages.add(msg);
					                }
					                if (previousMessages.size() > 0) {
					                    publish(previousMessages.get(previousMessages.size()-1));
					                }
					                br.close();
					            } catch (Exception ex) {
					                System.err.println(TAG + " - " + ex + ": " + ex.getMessage());
					            }
							}
						}
						wk.reset();
					}
					
				} catch (InterruptedException iex) {
				} catch (Exception ex) {
					System.err.println(TAG + " - " + ex + ": " + ex.getMessage());
				}
				
				return null;
			}
			
			@Override
			protected void process(List<String> list) {
				for (String s : list) {
					listData.addElement(s);
				}
			}
			
		};
		worker.execute();
		
		window.addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				worker.cancel(true);
				window.dispose();
				if (parentList != null) {
					parentList.removeSelectionInterval(0, parentList.getSelectedIndex());
				}
			}
			
		});
	}
	
	private static DefaultListModel<String> loadMessagesForSubscription(Subscription subscription) {
		DefaultListModel<String> listData = new DefaultListModel<String>();

        try {
        	String fileName = Constants.CONVERSATIONS_PATH + subscription.getSubscriptionFileName();
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = br.readLine()) != null) {
                listData.addElement(line);
            }
            br.close();
        } catch (Exception ex) {
			System.err.println(TAG + " - " + ex + ": " + ex.getMessage());
        }
		
		return listData;
	}
}