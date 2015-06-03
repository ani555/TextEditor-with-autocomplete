import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;
public class TextEditor {
    TrieST trie=new TrieST();
    private String[] result;
    private int count=1,spaces,words;
    int index=0;
    Object[] data=null;
    JFrame frame;
    String text="";
    public class SuggestionPanel {
        private JList list;
        private JPopupMenu popupMenu;
        private String subWord;
        private final int insertionPosition;
        
        public SuggestionPanel(JTextArea textarea, int position, String subWord, Point location) {
            this.insertionPosition = position;
            this.subWord = subWord;
            popupMenu = new JPopupMenu();
            popupMenu.removeAll();
            popupMenu.setOpaque(false);
            popupMenu.setBorder(null);
            popupMenu.add(list = createSuggestionList(position, subWord), BorderLayout.CENTER);
            popupMenu.show(textarea, location.x, textarea.getBaseline(0, 0) + location.y);
        }

        public void hide() {
            popupMenu.setVisible(false);
            if (suggestion == this) {
                suggestion = null;
            }
        }

        private JList createSuggestionList(final int position, final String subWord) {
            int searchres=trie.search(subWord);
            data=new Object[trie.str.size()+1];
            //System.out.println("hello ");
            if(searchres==1)
            {   
                System.out.println("creating suggestions "+trie.str.size());
            for (int i = 0; i < trie.str.size(); i++) {
                
                System.out.println(trie.str.get(i));
                    data[i]=trie.str.get(i);
            }
            trie.str.clear();
            }
            JList list = new JList(data);  
            list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setSelectedIndex(0);
            list.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        insertSelection();
                    }
                }
            });
            return list;
        }

        public boolean insertSelection() {
            if (list.getSelectedValue() != null) {
                try {
                    final String selectedSuggestion = ((String) list.getSelectedValue()).substring(subWord.length());
                    textarea.getDocument().insertString(insertionPosition, selectedSuggestion, null);
                    return true;
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
                hideSuggestion();
            }
            return false;
        }

        public void moveUp() {
            int index = Math.min(list.getSelectedIndex() - 1, 0);
            selectIndex(index);
        }

        public void moveDown() {
            int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
            selectIndex(index);
        }

        private void selectIndex(int index) {
            final int position = textarea.getCaretPosition();
            list.setSelectedIndex(index);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textarea.setCaretPosition(position);
                };
            });
        }
    }

    private SuggestionPanel suggestion;
    private JTextArea textarea;

    protected void showSuggestionLater() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                showSuggestion();
            }

        });
    }

    protected void showSuggestion() {
        hideSuggestion();
        final int position = textarea.getCaretPosition();
        Point location;
        try {
            location = textarea.modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            e2.printStackTrace();
            return;
        }
        
        text = textarea.getText();
        text=text.toLowerCase();
        
        for(int i=0;i<text.length();i++)
        {
            if(text.charAt(i)==' ' || text.charAt(i)=='\n')
            {
                text=text.replace('\n',' ');
                index++;
                //text=textarea.getText();
            }
            
        }
        System.out.println("spaces="+index);
        result=text.split(" ");
        if(index>0)
        {
            System.out.println("searching "+result[index-1]+" "+result[index-1].length());
            if(trie.search(result[index-1])==0)
            {
                System.out.println("inserting "+result[index-1]);
                trie.insert(result[index-1]);
            }
        }
        index=0;
        int start = Math.max(0, position - 1);
        while (start > 0) {
            if (!Character.isWhitespace(text.charAt(start))) {
                start--;
            } else {
                start++;
                break;
            }
        }
        if (start > position) {
            return;
        }
        final String subWord = text.substring(start, position);
        if (subWord.length() < 2) {
            return;
        }
        suggestion = new SuggestionPanel(textarea, position, subWord, location);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textarea.requestFocusInWindow();
            }
        });
    }

    private void hideSuggestion() {
        if (suggestion != null) {
            suggestion.hide();
        }
    }

    protected void buildUI() {
        frame = new JFrame();
        frame.setTitle("TextEditor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menubar=new JMenuBar();
        JMenu menu=new JMenu("File");
        JMenuItem open=new JMenuItem("Open File");
        JMenuItem save=new JMenuItem("Save");
        open.addActionListener(new LoadFileListener());
        save.addActionListener(new SaveFileListener());
        menu.add(open);
        menu.add(save);
        menubar.add(menu);
        frame.setJMenuBar(menubar);
        JPanel panel = new JPanel(new BorderLayout());
        textarea = new JTextArea(24, 80);
        JScrollPane scroller=new JScrollPane(textarea);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        textarea.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        textarea.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (suggestion != null) {
                        if (suggestion.insertSelection()) {
                            e.consume();
                            final int position = textarea.getCaretPosition();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textarea.getDocument().remove(position - 1, 1);
                                    } catch (BadLocationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
                    suggestion.moveDown();
                } else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
                    suggestion.moveUp();
                } else if (Character.isLetterOrDigit(e.getKeyChar())) {
                    showSuggestionLater();
                } else if (Character.isWhitespace(e.getKeyChar())) {
                    hideSuggestion();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }
        });
        //panel.add(textarea, BorderLayout.CENTER);
        //frame.add(panel);
        frame.getContentPane().add(panel,BorderLayout.CENTER);
        panel.add(scroller,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    class SaveFileListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser savefile=new JFileChooser();
            savefile.showSaveDialog(frame);
            fileSave(savefile.getSelectedFile());
        }
    }
    class LoadFileListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFileChooser loadfile=new JFileChooser();
            loadfile.showOpenDialog(frame);       
            fileOpen(loadfile.getSelectedFile());
        }
    }
    public void fileSave(File file)
    {
        try
        {
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            writer.write(textarea.getText());
            writer.close();
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }
    public void fileOpen(File file)
    {
        try
        {
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String textreader="";
            text="";
            while((textreader=reader.readLine())!=null)
            {
                textarea.append(textreader+"\n");
                text+=textreader;
                text+=" ";
            }
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }
        String[] res=text.split(" ");
        for(int i=0;i<res.length;i++)
        {
            if(trie.search(res[i])==0)
            {
                System.out.println("inserting "+res[i]);
                trie.insert(res[i]);
            }
        }
    }
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new TextEditor().buildUI();
            }
        });
    }

}
