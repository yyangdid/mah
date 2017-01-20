package mah.plugin.support.github.starred.sync;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import mah.common.util.IOUtils;
import mah.plugin.support.github.entity.GithubRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

public class SynchronizeRepositoryTask implements Callable<UpdateResult> {

    private final GithubRepositories starredRepositories;
    private Logger logger = LoggerFactory.getLogger(SynchronizeRepositoryTask.class);
    private String syncURL;

    public SynchronizeRepositoryTask(GithubRepositories starredRepositories, String syncURL) {
        if (starredRepositories == null) {
            throw new NullPointerException("User store wrapper is null");
        }
        this.starredRepositories = starredRepositories;
        this.syncURL = syncURL;
    }

    @Override
    public UpdateResult call() throws Exception {
        boolean needInit = false;
        String firstRepo;
        if (starredRepositories.size() <= 0) {
            needInit = true;
        } else {
            firstRepo = starredRepositories.getFirstRepositoryName();
            logger.info("The local latest store {}", firstRepo);
        }
        int pageIndex = 0;
        int addCount = 0;
        loop:
        while (true) {
            URL starredUrl;
            ++pageIndex;
            starredUrl = new URL(this.syncURL + "&page=" + pageIndex + "&per_page=5");
            HttpURLConnection httpURLConnection = (HttpURLConnection) starredUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.connect();
            InputStream inputStream = httpURLConnection.getInputStream();
            String content = IOUtils.toString(inputStream);
            JSONArray reps = JSON.parseArray(content);
            if ((reps.size() <= 0)) {
                break loop;
            }
            for (Object rep : reps) {
                JSONObject jsonObject = (JSONObject) rep;
                String name = jsonObject.getString("full_name");
                if (needInit) {
                    logger.debug("Add {}", name);
                    starredRepositories.addRepository(name, jsonObject.getString("description"));
                    addCount++;
                    continue;
                }
                if (starredRepositories.contains(name)) {
                    logger.info("The latest {}", name);
                    break loop;
                } else {
                    logger.debug("Add {}", name);
                    starredRepositories.updateNewRepository(name, jsonObject.getString("description"));
                    addCount++;
                }
            }
            httpURLConnection.disconnect();
        }
        logger.info("Updated {} repositorys", addCount);
        UpdateResult updateResult = new UpdateResult(addCount, starredRepositories);
        return updateResult;
    }

}

