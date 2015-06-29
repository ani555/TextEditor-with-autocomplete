import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
public class TextEditor {
    TrieST trie=new TrieST();
    ArrayList<String> result;
    private int count=1;
    int index=0,lastind;
    Object[] data=null;
    JFrame frame;
    JComboBox<String> fonts;
    JComboBox<String> themes;
    String[] themelist={"Default","Ocean Blue","Blood Red","Techie Green"};
    String[] fontlist=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
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
            int index = Math.max(list.getSelectedIndex() - 1, 0);
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
    public String getLastWord()
    {
        String s="";
        int i=text.length()-1,j;
        if(i>=1)
        {
        while((text.charAt(i)>='a' && text.charAt(i)<='z') && i>1)
        {
            i--;
        }
        while((text.charAt(i)<'a' || text.charAt(i)>'z') && i>1)
        {
            i--;
        }
        j=i-1;
        System.out.println(i+" "+j);
        if(j>=0)
        {
        while(j>=0 &&(text.charAt(j)>='a' && text.charAt(j)<='z'))
        {
            j--;
        }
        j++;
        while(j<=i)
        {
            s+=text.charAt(j);
            j++;
        }
        }
       }
        return s;
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
        String s=getLastWord();
        if(s!=null)
        {
            System.out.println("searching "+s+" "+trie.isPresent(s));
            if(trie.isPresent(s)==0)
            {
                System.out.println("inserting "+s);
                trie.insert(s);
            }
        }
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
        JMenu filemenu=new JMenu("File");
        JMenu editmenu=new JMenu("Edit");
        JMenu viewmenu=new JMenu("View"); 
        JMenuItem open=new JMenuItem("Open File");
        JMenuItem save=new JMenuItem("Save");
        SpinnerModel sizeModel=new SpinnerNumberModel(24,8,100,1);
        JSpinner fontsize=new JSpinner(sizeModel);
        fontsize.addChangeListener(new SizeChangeListener());
        open.addActionListener(new LoadFileListener());
        save.addActionListener(new SaveFileListener());
        fonts=new JComboBox<String>(fontlist);
        fonts.addActionListener(new FontChangeListener());
        themes=new JComboBox<String>(themelist);
        themes.addActionListener(new ThemeChangeListener());
        filemenu.add(open);
        filemenu.add(save);
        menubar.add(filemenu);
        menubar.add(editmenu);
        menubar.add(viewmenu);
        menubar.add(fonts);
        menubar.add(fontsize);
        menubar.add(themes);
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
        frame.getContentPane().add(panel,BorderLayout.CENTER);
        panel.add(scroller,BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
    class ThemeChangeListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JComboBox cb=(JComboBox)e.getSource();
            String style=(String)cb.getSelectedItem();
            if(style.equals("Default"))
            {
                textarea.setBackground(Color.white);
                textarea.setForeground(Color.black);
            }
            else if(style.equals("Ocean Blue"))
            {
                textarea.setBackground(Color.cyan);
                textarea.setForeground(Color.blue);
            }
            else if(style.equals("Blood Red"))
            {
                textarea.setBackground(Color.darkGray);
                textarea.setForeground(Color.red);
            }
            else if(style.equals("Techie Green"))
            {
                textarea.setBackground(Color.darkGray);
                textarea.setForeground(Color.green);
            }
        }
    }
    class FontChangeListener implements ActionListener
{
    @Override
    public void actionPerformed(ActionEvent e)
    {
        JComboBox cb=(JComboBox)e.getSource();
        int size=textarea.getFont().getSize();
        String fontName=(String)cb.getSelectedItem();
        textarea.setFont(new Font(fontName,Font.PLAIN,size));
    }
}
class SizeChangeListener implements ChangeListener
{
    @Override
    public void stateChanged(ChangeEvent e)
    {
        JSpinner sp=(JSpinner)e.getSource();
        int sz=(Integer)sp.getValue();
        Font font=new Font(textarea.getFont().getFontName(),Font.PLAIN,sz);
        textarea.setFont(font);
    }
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
        updateTrieOnFileOpen(text);
    }
    public void updateTrieOnFileOpen(String S)
    {
        S=S.toLowerCase();
        int i=0;
        String word="";
        while(i<S.length())
        {
            while(i<S.length() && (S.charAt(i)>='a' && S.charAt(i)<='z'))
            {
                word+=S.charAt(i); i++;
            }
            if(trie.isPresent(word)==0)
            {
                System.out.println("inserting "+word);
                trie.insert(word);
            }
            word="";
            while(i<S.length() && (S.charAt(i)<'a' || S.charAt(i)>'z'))
                i++;
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
