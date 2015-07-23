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
public class TextEditorNew {
    ArrayList<TrieST> trie=new ArrayList<TrieST>();
    TrieST dict=new TrieST();
    ArrayList<String> result;
    int index=0,lastind;
    Object[] data=null;
    JFrame frame;
    JTextArea findstr;
    JScrollPane scroller;
    JTabbedPane jtp;
    int tabs;
    JComboBox<String> fonts;
    JComboBox<String> themes;
    JSpinner fontsize;
    String[] themelist={"Default","Ocean Blue","Blood Red","Techie Green"};
    String[] fontlist=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    String text="";
    String orgtext="";
    String substr="";
    ArrayList<Integer> wordpos=new ArrayList<Integer>();
    ArrayList<String> textthemes=new ArrayList<String>();
    ArrayList<Integer> fontindex=new ArrayList<Integer>();
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
            int searchres=trie.get(jtp.getSelectedIndex()).search(subWord);
            data=new Object[trie.get(jtp.getSelectedIndex()).str.size()+1];
            System.out.println("searchres="+searchres+" "+subWord);
            if(searchres>0)
            {   
                System.out.println("creating suggestions "+trie.get(jtp.getSelectedIndex()).str.size());
            for (int i = 0; i < trie.get(jtp.getSelectedIndex()).str.size(); i++) {
                
                System.out.println(trie.get(jtp.getSelectedIndex()).str.get(i));
                    data[i]=trie.get(jtp.getSelectedIndex()).str.get(i);
            }
            trie.get(jtp.getSelectedIndex()).str.clear();
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
                    textarea.get(jtp.getSelectedIndex()).getDocument().insertString(insertionPosition, selectedSuggestion, null);
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
            final int position = textarea.get(jtp.getSelectedIndex()).getCaretPosition();
            list.setSelectedIndex(index);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    textarea.get(jtp.getSelectedIndex()).setCaretPosition(position);
                };
            });
        }
    }

    private SuggestionPanel suggestion;
    private ArrayList<JTextArea> textarea=new ArrayList<JTextArea>();
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
        int i=textarea.get(jtp.getSelectedIndex()).getCaretPosition()-1,j;
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
        final int position = textarea.get(jtp.getSelectedIndex()).getCaretPosition();
        System.out.println("position="+position);
        Point location;
        try {
            location = textarea.get(jtp.getSelectedIndex()).modelToView(position).getLocation();
        } catch (BadLocationException e2) {
            e2.printStackTrace();
            return;
        }
        
        text = textarea.get(jtp.getSelectedIndex()).getText();
        text=text.toLowerCase();
        String s=getLastWord();
        System.out.println(s);
        if(s!=null)
        {
            System.out.println("searching "+s+trie.get(jtp.getSelectedIndex()).isPresent(s));
            if(trie.get(jtp.getSelectedIndex()).isPresent(s)==0)
            {
                System.out.println("inserting "+s);
                trie.get(jtp.getSelectedIndex()).insert(s);
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
        suggestion = new SuggestionPanel(textarea.get(jtp.getSelectedIndex()), position, subWord, location);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                textarea.get(jtp.getSelectedIndex()).requestFocusInWindow();
            }
        });
    }

    private void hideSuggestion() {
        if (suggestion != null) {
            suggestion.hide();
        }
    }
    protected void buildUI() {
        populateDict();
        frame = new JFrame();
        frame.setTitle("TextEditor");
        ImageIcon frameicon=new ImageIcon("TextEditor.png");
        frame.setIconImage(frameicon.getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menubar=new JMenuBar();
        JMenu filemenu=new JMenu("File");
        JMenu editmenu=new JMenu("Edit");
        JMenu viewmenu=new JMenu("View"); 
        JMenu appmenu=new JMenu("Tools");
        ImageIcon newfileicon=new ImageIcon("NewFile-icon.png");
        JMenuItem newfile=new JMenuItem("New File",newfileicon);
        ImageIcon openicon=new ImageIcon("Open-file-icon.png");
        JMenuItem open=new JMenuItem("Open File",openicon);
        ImageIcon saveicon=new ImageIcon("Save-file-icon.png");
        JMenuItem save=new JMenuItem("Save",saveicon);
        ImageIcon clearicon=new ImageIcon("Clear-icon.png");
        JMenuItem clearhighlights=new JMenuItem("Clear Highlights",clearicon);
        ImageIcon findicon=new ImageIcon("Find-icon.png");
        JMenuItem find=new JMenuItem("Find",findicon);
        ImageIcon replaceicon=new ImageIcon("Replace-icon.png");
        JMenuItem replace=new JMenuItem("Replace",replaceicon);
        ImageIcon spellicon=new ImageIcon("Spell-icon.jpg");
        JMenuItem spellcheck=new JMenuItem("Spell Check",spellicon);
        JMenuItem lrsapp=new JMenuItem("Longest Repeated Substring");
        JMenuItem lpsapp=new JMenuItem("Longest Palindromic Substring");
        SpinnerModel sizeModel=new SpinnerNumberModel(24,8,100,1);
        fontsize=new JSpinner(sizeModel);
        fontsize.addChangeListener(new SizeChangeListener());
        newfile.addActionListener(new NewFileListener());
        open.addActionListener(new LoadFileListener());
        save.addActionListener(new SaveFileListener());
        clearhighlights.addActionListener(new ClearHighlightsListener());
        find.addActionListener(new FindListener());
        replace.addActionListener(new ReplaceListener());
        spellcheck.addActionListener(new SpellCheckListener());
        lrsapp.addActionListener(new LRSListener());
        lpsapp.addActionListener(new LPSListener());
        fonts=new JComboBox<String>(fontlist);
        fonts.addActionListener(new FontChangeListener());
        themes=new JComboBox<String>(themelist);
        themes.addActionListener(new ThemeChangeListener());
        filemenu.add(newfile);
        filemenu.add(open);
        filemenu.add(save);
        editmenu.add(clearhighlights);
        editmenu.add(find);
        editmenu.add(replace);
        editmenu.add(spellcheck);
        appmenu.add(lrsapp);
        appmenu.add(lpsapp);
        menubar.add(filemenu);
        menubar.add(editmenu);
        menubar.add(viewmenu);
        menubar.add(appmenu);
        menubar.add(fonts);
        menubar.add(fontsize);
        menubar.add(themes);
        frame.setJMenuBar(menubar);
        jtp=new JTabbedPane();
        jtp.addChangeListener(new ChangeListener(){
           @Override
           public void stateChanged(ChangeEvent e)
           {
               if(jtp.getSelectedIndex()>=0)
               {
                System.out.println(fontindex.get(jtp.getSelectedIndex()));
                System.out.println(jtp.getSelectedIndex());
                fonts.setSelectedIndex(fontindex.get(jtp.getSelectedIndex()));
                fontsize.setValue(textarea.get(jtp.getSelectedIndex()).getFont().getSize());
                themes.setSelectedItem(textthemes.get(jtp.getSelectedIndex()));
               }
           }
        });
        //System.out.println(jtp.getTabCount());
        frame.getContentPane().add(jtp,BorderLayout.CENTER);
        frame.setSize(600,500);
        //frame.pack();
        frame.setVisible(true);
    }
    public JPanel buildTextPanel()
    {
        trie.add(new TrieST());
        JPanel panel = new JPanel(new BorderLayout());
        textarea.add(new JTextArea(24, 80));
        textthemes.add("Default");
        fontindex.add(0);
        scroller=new JScrollPane(textarea.get(tabs));
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        panel.add(scroller,BorderLayout.CENTER);
        textarea.get(tabs).setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        textarea.get(tabs).addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (suggestion != null) {
                        if (suggestion.insertSelection()) {
                            e.consume();
                            final int position = textarea.get(jtp.getSelectedIndex()).getCaretPosition();
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        textarea.get(jtp.getSelectedIndex()).getDocument().remove(position - 1, 1); //removes the "\n" on suggestion selection
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
        return panel;
    }
class CloseTabButton extends JPanel implements ActionListener {
  private JTabbedPane pane;
  public CloseTabButton(JTabbedPane pane, int index) {
    this.pane = pane;
    setOpaque(false);
    add(new JLabel(
        pane.getTitleAt(index),
        pane.getIconAt(index),
        JLabel.LEFT));
    Icon closeIcon = new CrossIcon();
    JButton btClose = new JButton(closeIcon);
    btClose.setPreferredSize(new Dimension(
        closeIcon.getIconWidth(), closeIcon.getIconHeight()));
    add(btClose);
    btClose.addActionListener(this);
    pane.setTabComponentAt(index, this);
  }
  @Override
  public void actionPerformed(ActionEvent e) {
    int i = pane.indexOfTabComponent(this);
    if (i != -1) {
      pane.remove(i);
      trie.remove(i);
      textarea.remove(i);
      textthemes.remove(i);
      fontindex.remove(i);
    }
  }
}

class CrossIcon implements Icon {
  @Override
  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(Color.RED);
    g.drawLine(6, 6, getIconWidth() - 7, getIconHeight() - 7);
    g.drawLine(getIconWidth() - 7, 6, 6, getIconHeight() - 7);
  }
  @Override
  public int getIconWidth() {
    return 17;
  }
  @Override
  public int getIconHeight() {
    return 17;
  }
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
                textarea.get(jtp.getSelectedIndex()).setBackground(Color.white);
                textarea.get(jtp.getSelectedIndex()).setForeground(Color.black);
                textarea.get(jtp.getSelectedIndex()).setCaretColor(Color.black);
                textthemes.remove(jtp.getSelectedIndex());
                textthemes.add(jtp.getSelectedIndex(), "Default");
            }
            else if(style.equals("Ocean Blue"))
            {
                textarea.get(jtp.getSelectedIndex()).setBackground(Color.black);
                textarea.get(jtp.getSelectedIndex()).setForeground(Color.cyan);
                textarea.get(jtp.getSelectedIndex()).setCaretColor(Color.MAGENTA);
                textthemes.remove(jtp.getSelectedIndex());
                textthemes.add(jtp.getSelectedIndex(), "Ocean Blue");
            }
            else if(style.equals("Blood Red"))
            {
                textarea.get(jtp.getSelectedIndex()).setBackground(Color.white);
                textarea.get(jtp.getSelectedIndex()).setForeground(Color.red);
                textarea.get(jtp.getSelectedIndex()).setCaretColor(Color.black);
                textthemes.remove(jtp.getSelectedIndex());
                textthemes.add(jtp.getSelectedIndex(), "Blood Red");
            }
            else if(style.equals("Techie Green"))
            {
                textarea.get(jtp.getSelectedIndex()).setBackground(Color.black);
                textarea.get(jtp.getSelectedIndex()).setForeground(Color.green);
                textarea.get(jtp.getSelectedIndex()).setCaretColor(Color.white);
                textthemes.remove(jtp.getSelectedIndex());
                textthemes.add(jtp.getSelectedIndex(), "Techie Green");
            }
        }
    }
    class FontChangeListener implements ActionListener
{
    @Override
    public void actionPerformed(ActionEvent e)
    {
        JComboBox cb=(JComboBox)e.getSource();
        int size=textarea.get(jtp.getSelectedIndex()).getFont().getSize();
        String fontName=(String)cb.getSelectedItem();
        textarea.get(jtp.getSelectedIndex()).setFont(new Font(fontName,Font.PLAIN,size));
        fontindex.remove(jtp.getSelectedIndex());
        fontindex.add(jtp.getSelectedIndex(),cb.getSelectedIndex());
    }
}
class SizeChangeListener implements ChangeListener
{
    @Override
    public void stateChanged(ChangeEvent e)
    {
        JSpinner sp=(JSpinner)e.getSource();
        int sz=(Integer)sp.getValue();
        Font font=new Font(textarea.get(jtp.getSelectedIndex()).getFont().getFontName(),Font.PLAIN,sz);
        textarea.get(jtp.getSelectedIndex()).setFont(font);
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
    class NewFileListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String tabname="untitled";
            createNewTab(tabname);
        }
    }
    class ClearHighlightsListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            textarea.get(jtp.getSelectedIndex()).setText(textarea.get(jtp.getSelectedIndex()).getText());
        }
    }
    class ReplaceListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JFrame repframe=new JFrame("Replace");
            JTextField oldtf=new JTextField(20);
            JLabel oldlb=new JLabel("String to be replaced:");
            JTextField newtf=new JTextField(20);
            JLabel newlb=new JLabel("Replacement:");
            JButton done=new JButton("Done");
            Box repbox=new Box(BoxLayout.Y_AXIS);
            repbox.add(oldlb);
            repbox.add(oldtf);
            repbox.add(newlb);
            repbox.add(newtf);
            repbox.add(done);
            repframe.getContentPane().add(BorderLayout.CENTER,repbox);
            repframe.pack();
            repframe.setVisible(true);
            done.addActionListener(new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    String oldstr=oldtf.getText();
                    String newstr=newtf.getText();
                    orgtext=textarea.get(jtp.getSelectedIndex()).getText();
                    orgtext=orgtext.replaceAll(oldstr, newstr);
                    textarea.get(jtp.getSelectedIndex()).setText(orgtext);
                }
            });
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
            findbt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mEvt) {
            findbt.setToolTipText("Find first occurance of substring");
            }});
            JButton nextoccbt=new JButton("Next");
            nextoccbt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mEvt) {
            nextoccbt.setToolTipText("Next occurance of substring");
            }});
            JButton clearbt=new JButton("Clear");
            clearbt.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mEvt) {
            clearbt.setToolTipText("Clear Highlights");
            }});
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
            textarea.get(jtp.getSelectedIndex()).setBackground(textarea.get(jtp.getSelectedIndex()).getBackground());
            textarea.get(jtp.getSelectedIndex()).setText(textarea.get(jtp.getSelectedIndex()).getText());
            findstr.setText("");
            currpos=0;
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
            System.out.println(substr.length());
            String S=textarea.get(jtp.getSelectedIndex()).getText();
            if(substr.length()!=0 && S.length()!=0)
            {
            for(int pos=-1;(pos=S.indexOf(substr,pos+1))!=-1;)
            {
                System.out.println(pos);
                wordpos.add(pos);
            }
            if(wordpos.size()>0)
            {
            int st=wordpos.get(currpos);
            int fin=st+substr.length();
            try{
            Highlighter highlighter=textarea.get(jtp.getSelectedIndex()).getHighlighter();
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
            if(substr.length()!=0 && wordpos.size()!=0 && textarea.get(jtp.getSelectedIndex()).getText().length()!=0)
            {
            if(currpos+1<wordpos.size())
            currpos++;
            int st=wordpos.get(currpos);
            int fin=st+substr.length();
            try{
            Highlighter highlighter=textarea.get(jtp.getSelectedIndex()).getHighlighter();
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
    class SpellCheckListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            String S=textarea.get(jtp.getSelectedIndex()).getText().toLowerCase();
            int i=0;
            String word="";
            while(i<S.length())
            {
            while(i<S.length() && (S.charAt(i)>='a' && S.charAt(i)<='z'))
            {
                word+=S.charAt(i); i++;
            }
            if(dict.isPresent(word)==0)
            {
                System.out.println("not in dict "+word);
                int fin=i;
                int st=fin-word.length();
                try
                {
                Highlighter highlighter=textarea.get(jtp.getSelectedIndex()).getHighlighter();
                HighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
                highlighter.addHighlight(st, fin, painter);
                }
                catch(BadLocationException ex)
                {
                    ex.printStackTrace();
                }
            }
            word="";
            while(i<S.length() && (S.charAt(i)<'a' || S.charAt(i)>'z'))
                i++;
            }
        }
    }
    class LRSListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            orgtext=textarea.get(jtp.getSelectedIndex()).getText();
            String lrs=LRS.lrs(orgtext);    
            highlightText(orgtext,lrs);
            JOptionPane.showMessageDialog(frame,"Longest Repeated Substring is:"+lrs);
        }
    }
class LPSListener implements ActionListener
{
    @Override
    public void actionPerformed(ActionEvent e)
    {
        orgtext=textarea.get(jtp.getSelectedIndex()).getText();
        String lps=LPS.longestPalindrome(orgtext);
        highlightText(orgtext,lps);
        JOptionPane.showMessageDialog(frame,"Longest Palindromic Substring is:"+lps);
    }
}
public void highlightText(String text,String word)
    {
        for(int pos=-1;(pos=text.indexOf(word,pos+1))!=-1;)
            {
                try{
                Highlighter highlighter=textarea.get(jtp.getSelectedIndex()).getHighlighter();
                HighlightPainter painter=new DefaultHighlighter.DefaultHighlightPainter(Color.orange);
                highlighter.addHighlight(pos, pos+word.length(), painter);
                System.out.println("pos="+pos+word);
                }
                catch(BadLocationException ex)
                {
                    ex.printStackTrace();
                }    
            }
    }
    public void createNewTab(String tabname)
    {
            tabs=jtp.getTabCount();
            jtp.add(tabname,buildTextPanel());
            //System.out.println(jtp.getTabCount());
            new CloseTabButton(jtp,tabs);
    }
    public void fileSave(File file)
    {
        try
        {
            BufferedWriter writer=new BufferedWriter(new FileWriter(file));
            writer.write(textarea.get(jtp.getSelectedIndex()).getText());
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
            createNewTab(file.getName());
            BufferedReader reader=new BufferedReader(new FileReader(file));
            String textreader="";
            text="";
            textarea.get(tabs).setText(text);
            while((textreader=reader.readLine())!=null)
            {
                textarea.get(tabs).append(textreader+"\n");
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
            if(trie.get(tabs).isPresent(word)==0)
            {
                System.out.println("inserting "+word);
                trie.get(tabs).insert(word);
            }
            word="";
            while(i<S.length() && (S.charAt(i)<'a' || S.charAt(i)>'z'))
                i++;
        }
    }
    private void populateDict()
    {
        String word="";
        try
        {
        BufferedReader reader=new BufferedReader(new FileReader("Dictionary.txt"));
        while((word=reader.readLine())!=null)
        {
            dict.insert(word);
        }
        }
        catch(Exception e)
        {
            System.out.println(word);
            e.printStackTrace();
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
                new TextEditorNew().buildUI();
            }
        });
    }

}
