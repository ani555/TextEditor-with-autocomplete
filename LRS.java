import java.util.*;
class LRS {

    public static String lcp(String a,String b)
    {
        int n=Math.min(a.length(), b.length());
        for(int i=0;i<n;i++)
        {
            if(a.charAt(i)!=b.charAt(i))
                return a.substring(0,i);
        }
        return a.substring(0,n);
    }
    
    public static String lrs(String text)
    {
        int N=text.length();
        String[] suffixes=new String[N];
        for(int i=0;i<N;i++)
            suffixes[i]=text.substring(i, N);
        Arrays.sort(suffixes);
        String lrs="";
        for(int i=0;i<N-1;i++)
        {
            // System.out.println("suffix-"+suffixes[i]);
            String x=lcp(suffixes[i],suffixes[i+1]);
            if(x.length()>lrs.length())
                lrs=x;
        }
        return lrs;
    }
}
