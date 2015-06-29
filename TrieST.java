import java.util.*;
public class TrieST
{
    //public  char[] k;
    //public String s="";
    public ArrayList<String> str;
    private static class Node{
        private int value;
        private Node[] next=new Node[R];
    }
    private static final int R=26;
    private Node root;
    private int count;
    TrieST()
    {
        root=getNode();
        count=0;
        str=new ArrayList<String>();
    }
    Node getNode()
    {
        Node node =new Node();
        if(node!=null)
        {
            node.value=0;
            for(int i=0;i<26;i++)
            {
                node.next[i]=null;
            }
        }
        return node;
    }
    public void insert(String key)
    {
        Node node=root;
        count++;
        int level;
        int index;
        for(level=0;level<key.length();level++)
        {
            index=((int)key.charAt(level)-(int)'a');
            System.out.println(index);
            if(node.next[index]==null)
            {
                node.next[index]=getNode();
            }
            node=node.next[index];
        }
        node.value=count;
            
    }
    public int isPresent(String key)
    {
        Node x=root;
        int level;
        int index;
        for(level=0;level<key.length();level++)
        {
            index=((int)key.charAt(level)-(int)'a');
            //System.out.println(index);
            if(x.next[index]==null)
            {
                return 0;
            }
                x=x.next[index];
        }
        return 1;
    }
    public int search(String key)
    {
        str.clear();
        Node x=root;
        int level;
        int index;
        int flag=0;
        for(level=0;level<key.length();level++)
        {
            index=((int)key.charAt(level)-(int)'a');
            //System.out.println(index);
            if(index>26 || index<0)
                return 0;
            if(x.next[index]==null)
            {
                return 0;
            }
                x=x.next[index];
        }
        for(int i=0;i<26;i++)
        {
            if(x.next[i]!=null)
            {
                flag=1;
		break;
            }
        }
        if(flag==1)
	{
            dfs(x,key,level,level,key);			//prints all the words that contains the given prefix
            if(flag==1 && x.value==0)
                return flag;							//if prefix terminates on a intermediate node with value=0
            return x.value;					//if prefix terminates on a intermediate node with value!=0
	}
	else
            return flag;
    }
    public void dfs(Node x,String key,int pre,int level,String ref)
    {
        if(x.value!=0 && level>pre)
        {
            str.add(key);
            System.out.println("Dict:"+key);
        }
        for(int i=0;i<26;i++)
        {
            if(x.next[i]!=null)
            {
                char ch=(char)('a'+i);
                key+=ch;
                dfs(x.next[i],key,pre,level+1,ref);
                key=ref;
            }
        }
    }
}