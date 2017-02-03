
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by amit on 1/2/17.
 */
public class GitRepoReader {

    public static void main(String args[]){


        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(System.getProperty("user.dir") + "/application.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String url=properties.getProperty("ReposUrl");
        StringBuilder orgUrls =callApi(url);

        List<Repo> topFiveRepo=topRepository(orgUrls);
        StringBuilder contributorBuilder;
        System.out.println("Top 5 most popular repositories of a firm based on the number of forks");
        System.out.println("-----------------------------------------------------------------------");
        for (Repo r: topFiveRepo) {
            System.out.println("\tRepository Name : "+r.repoName);
            contributorBuilder=callApi(r.getContributors());
            contributorsCount(contributorBuilder);
        }
    }
/** takes StringBuilder returned by callApi method and returns list of top 5 repos*/

    public static List<Repo> topRepository(StringBuilder topRepositoryBuilder){
        /** pattern for all the attributes which we want to capture */
        Pattern repoName=Pattern.compile("\"full_name\":\\s+\"([^\"]+).*");
        Pattern forkCount=Pattern.compile("\"forks_count\":\\s+(\\d+).*");
        Pattern contributors=Pattern.compile("\"contributors_url\":\\s+\"([^\"]+).*");

        Matcher repoNameMatch;
        Matcher forkCountMatch;
        Matcher contributorsMatch;

        /**List of the Repositories to store all the repository in the firm*/
        List<Repo> repoList=new ArrayList<>();
        String repoNametemp=null;
        String contributorstemp=null;
        int forkCounttemp=-1;

        String[] lines = topRepositoryBuilder.toString().split(System.getProperty("line.separator"));

        for(String line: lines){

            repoNameMatch=repoName.matcher(line);
            if(repoNameMatch.find()){
                // System.out.println(repoNameMatch.group(1));
                repoNametemp=repoNameMatch.group(1);
            }

            contributorsMatch=contributors.matcher(line);
            if(contributorsMatch.find()){
                //System.out.println(contributorsMatch.group(1));
                contributorstemp=contributorsMatch.group(1);
            }

            forkCountMatch=forkCount.matcher(line);
            if(forkCountMatch.find()){
                //System.out.println(forkCountMatch.group(1));
                forkCounttemp= Integer.parseInt(forkCountMatch.group(1));

            }
            /** Once we get the repo name , contributors url and fork count of the repo, then store it into the list*/
            if(repoNametemp!=null && contributorstemp !=null && forkCounttemp!=-1){
                Repo newRepo=new Repo(repoNametemp,contributorstemp,forkCounttemp);
                repoList.add(newRepo);
                repoNametemp=null;
                contributorstemp=null;
                forkCounttemp=-1;
            }
        }

        /** Sorting the list on the basis of fork count and putting Top 5 Repository on another list*/
        Collections.sort(repoList);
        List<Repo> topFiveRepo=new ArrayList<>();
        int count=5;
        for (Repo r: repoList) {
            //System.out.println(r.repoName+" "+r.contributors+" "+r.forkCount);
            if (count-->0){
                topFiveRepo.add(r);
            }
        }

        return topFiveRepo;
    }

/** takes StringBuilder returned by callApi */
    public static void contributorsCount(StringBuilder contributorBuilder){
        String[] lines = contributorBuilder.toString().split(System.getProperty("line.separator"));
        Pattern login=Pattern.compile("\"login\":\\s+\"([^\"]+).*");
        Pattern contributions=Pattern.compile("\"contributions\":\\s+(\\d+).*");

        Matcher loginMatch;
        Matcher contributionsMatch;

        List<Committee> committeeList=new ArrayList<>();
        String loginMatchtemp=null;
        int contributionsMatchtemp=-1;
        for(String line: lines){
            loginMatch=login.matcher(line);
            if(loginMatch.find()){
                //System.out.println(loginMatch.group(1));
                loginMatchtemp=loginMatch.group(1);
            }


            contributionsMatch=contributions.matcher(line);
            if(contributionsMatch.find()){
                //System.out.println(contributionsMatch.group(1));
                contributionsMatchtemp= Integer.parseInt(contributionsMatch.group(1));

            }

            if(loginMatchtemp!=null && contributionsMatchtemp!=-1){
                Committee newCommittee=new Committee(loginMatchtemp,contributionsMatchtemp);
                committeeList.add(newCommittee);
                loginMatchtemp=null;
                contributionsMatchtemp=-1;
            }
        }

        Collections.sort(committeeList);
        List<Committee> topCommittee=new ArrayList<>();
        int count=5;
        for (Committee c: committeeList) {
            if (count-->0){
                topCommittee.add(c);
            }
        }

        System.out.println("\t\tTop 3 committees and their commit count");
        System.out.println("\t\t---------------------------------------");
        for (Committee c:topCommittee) {
            System.out.println("\t\t\tCommittee Name : "+c.committeeName+"\t\t\tCommit Count : "+c.commitCount);
        }
        System.out.println();
    }


/** takes git urls of the firm and calls the api and returns result as stringBuffer*/

    public static StringBuilder callApi(String url){

        /** Reading Properties from configuration file*/
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(System.getProperty("user.dir") + "/application.conf"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String username = properties.getProperty("User");
        String password = properties.getProperty("Password");

        /** Preparing curl command into a string array*/
        String[] command = {"curl", "-H", "Accept:application/json", "-u", username + ":" + password, url};

        /** Using ProcessBuilder to execute above curl command*/
        ProcessBuilder process = new ProcessBuilder(command);
        Process p;
        StringBuilder builder = new StringBuilder();
        try {
            p = process.start();

            /**Reading output of the process into a BufferReader and storing each line of it into StringBuilder*/
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }

        } catch (IOException e) {
            System.out.print("error");
            e.printStackTrace();
        }

        return builder;
    }

}
/** class contains the repository name , contributors url and fork count of the repository*/

class Repo implements Comparable<Repo>{
    String repoName;
    String contributors;
    int forkCount;
    Repo(String repoName, String contributors, int forkCount){
        this.repoName=repoName;
        this.contributors=contributors;
        this.forkCount=forkCount;
    }

    public String getContributors(){
        return contributors;
    }

    public int compareTo(Repo repo) {
        return repo.forkCount-this.forkCount;
    }

}
/** class contains committee Name and their commit count */

class Committee implements Comparable<Committee>{

    String committeeName;
    int commitCount;

    Committee(String committeeName, int commitCount){
        this.committeeName=committeeName;
        this.commitCount=commitCount;
    }

    public int compareTo(Committee committee) {
        return committee.commitCount-this.commitCount;
    }
}
