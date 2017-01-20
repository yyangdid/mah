package mah.plugin.support.orgnote.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import mah.plugin.support.orgnote.util.IOUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zgq on 16-10-19.
 */
public class Config {
    private List<String> filenames=new ArrayList<>();
    private int fnIndex;
    private int nodeIndex;

    public boolean contains(String filename){
        return filenames.contains(filename);
    }
    public List<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(List<String> filenames) {
        this.filenames = filenames;
    }

    public int getFnIndex() {
        return fnIndex;
    }

    public void setFnIndex(int fnIndex) {
        this.fnIndex = fnIndex;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }


    public String getCurrentFilename(){
        if (filenames.size() > 0) {
            return filenames.get(fnIndex);
        }
        return null;
    }

    public void addDataFilename(String s) {
        filenames.add(s);
    }

    public boolean dataEmpty() {
        return filenames.size()==0;
    }

    public static void update(Config config, List<JSONObject> list,String path) {
        if (config.getNodeIndex() >= list.size() - 1) {
            if (config.getFnIndex() + 1 >= config.getFilenames().size()) {
                config.setFnIndex(0);
            } else {
                config.setFnIndex(config.getFnIndex() + 1);
            }
            config.setNodeIndex(0);
        } else {
            config.setNodeIndex(config.getNodeIndex() + 1);
        }
        try {
            IOUtil.writeToFile(path, JSON.toJSONString(config));
        } catch (IOException e) {
            new RuntimeException(e);
        }
    }

    public static void update(String path){
        update(Config.config,Config.list,path);
    }

    public static void updateReviewList(String dataDir) {
        updateReviewList(Config.config,Config.list,dataDir);
    }
    private static Config config;
    private static List<JSONObject> list;
    public static final void setSource(Config config, List<JSONObject> list) {
       Config.config = config;
        Config.list = list;
    }

    public static void updateReviewList(Config config,List<JSONObject> list,String dataDir) {
        try {
            IOUtil.writeToFile(dataDir+"/"+config.getCurrentFilename(), JSON.toJSONString(list));
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}
