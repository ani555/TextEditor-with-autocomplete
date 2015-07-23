import java.util.*;
public class LPS {
    private static String preprocess(String s)
    {
        int n=s.length();
        String t;
        if(n==0) return "^$";
        else
        {
            t="^";
            for(int i=0;i<s.length();i++)
                t+="#"+s.charAt(i);
            t+="#$";
            return t;
        }
    }
    public static String longestPalindrome(String s)
    {
        String T=preprocess(s);
        int n=T.length();
        int[] P=new int[n];
        int C=0,R=0,i_mirror;
        for(int i=1;i<n-1;i++)
        {
            i_mirror=2*C-i;
            P[i]=(R>i)? Math.min(R-i,P[i_mirror]):0;
            while(T.charAt(i+P[i]+1)==T.charAt(i-P[i]-1))
            {
                P[i]++;
            }
            if(i+P[i]>R)
            {
               C=i;
               R=i+P[i];
            }
        }
        int centerid=0,maxlen=0;
        for(int i=1;i<n-1;i++)
        {
            if(P[i]>maxlen)
            {
                maxlen=P[i];
                centerid=i;
            }
        }
        return s.substring((centerid-1-maxlen)/2,(centerid-1-maxlen)/2+maxlen);
    }
    /*public static void main(String[] args)
    {
        System.out.println(LPS.longestPalindrome("babcbabcbaccba"));
    }*/
}
