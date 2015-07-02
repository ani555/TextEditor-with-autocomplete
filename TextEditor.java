import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import java.util.*;
import java.io.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
public class TextEditor {
    TrieST trie=new TrieST();
    ArrayList<String> result;
    int index=0,lastind;
    Object[] data=null;
    JFrame frame;
    JTextArea findstr;
    JComboBox<String> fonts;
    JComboBox<String> themes;
    String[] themelist={"Default","Ocean Blue","Blood Red","Techie Green"};
    String[] fontlist=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    String text="";
    String substr="";
    ArrayList<Integer> wordpos=new ArrayList<Integer>();
    int currpos;
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
            System.out.println("searchres="+searchres+" "+subWord);
            if(searchres>0)
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
        int i=textarea.getCaretPosition()-1,j;
        if(i>=0)
        {
        while((text.charAt(i)>='a' && text.charAt(i)<='z') && i>0)
        {
            i--;
        }
        while((text.charAt(i)<'a' || text.charAt(i)>'z') && i>0)
        {
            i--;
        }
        j=i-1;
        if(i==0) 
        j=0;
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
        System.out.println("position="+position);
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
            System.out.println("searching "+s+trie.isPresent(s));
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
        JMenuItem find=new JMenuItem("Find");
        SpinnerModel sizeModel=new SpinnerNumberModel(24,8,100,1);
        JSpinner fontsize=new JSpinner(sizeModel);
        fontsize.addChangeListener(new SizeChangeListener());
        open.addActionListener(new LoadFileListener());
        save.addActionListener(new SaveFileListener());
        find.addActionListener(new FindListener());
        fonts=new JComboBox<String>(fontlist);
        fonts.addActionListener(new FontChangeListener());
        themes=new JComboBox<String>(themelist);
        themes.addActionListener(new ThemeChangeListener());
        filemenu.add(open);
        filemenu.add(save);
        editmenu.add(find);
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
                textarea.setCaretColor(Color.black);
            }
            else if(style.equals("Ocean Blue"))
            {
                textarea.setBackground(Color.black);
                textarea.setForeground(Color.cyan);
                textarea.setCaretColor(Color.blue);
            }
            else if(style.equals("Blood Red"))
            {
                textarea.setBackground(Color.white);
                textarea.setForeground(Color.red);
                textarea.setCaretColor(Color.black);
            }
            else if(style.equals("Techie Green"))
            {
                textarea.setBackground(Color.black);
                textarea.setForeground(Color.green);
                textarea.setCaretColor(Color.white);
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
    class FindListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFrame editFrame=new JFrame("Find");
            findstr=new JTextArea(1,30);
            JScrollPane fsscroller=new JScrollPane(findstr);
            fsscroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
            editFrame.getContentPane().add(fsscroller,BorderLayout.CENTER);
            FlowLayout flow=new FlowLayout();
            JPanel buttonpanel=new JPanel(flow);
            JButton findbt=new JButton("Find");
            JButton nextoccbt=new JButton("Next");
            JButton clearbt=new JButton("Clear");
            findbt.addActionListener(new FindButtonListener());
            nextoccbt.addActionListener(new NextOccuranceListener());
            clearbt.addActionListener(new ClearButtonListener());
            buttonpanel.add(findbt);
            buttonpanel.add(nextoccbt);
            buttonpanel.add(clearbt);
            editFrame.getContentPane().add(buttonpanel,BorderLayout.SOUTH);
            editFrame.setVisible(true);
            //editFrame.setSize(300, 300);
            editFrame.pack();
        }
    }
    class ClearButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            textarea.setBackground(textarea.getBackground());
            textarea.setText(text);
            findstr.setText("");
            wordpos.clear();
        }
    }
    class FindButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            substr=findstr.getText();
            wordpos.clear();
            if(substr!=null)
            {
            for(int pos=-1;(pos=text.indexOf(substr,pos+1))!=-1;)
            {
                System.out.println(pos);
                wordpos.add(pos);
            }
            if(wordpos.size()>0)
            {
            int st=wordpos.get(currpos);
            int fin=st+substr.length();
            try{
            Highlighter highlighter=textarea.getHighlighter();
            HighlightPainter painter = 
             new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
            highlighter.addHighlight(st, fin, painter);
            }
            catch(BadLocationException ex)
            {
                ex.printStackTrace();
            }
            }
            }
        }
    } 
    class NextOccuranceListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if(substr!=null && currpos==0 && wordpos.size()!=0)
            {
            if(currpos+1<wordpos.size())
            currpos++;
            int st=wordpos.get(currpos);
            int fin=st+substr.length();
            try{
            Highlighter highlighter=textarea.getHighlighter();
            HighlightPainter painter = 
             new DefaultHighlighter.DefaultHighlightPainter(Color.pink);
            highlighter.addHighlight(st, fin, painter);
            }
            catch(BadLocationException ex)
            {
                ex.printStackTrace();
            }   
            }
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
