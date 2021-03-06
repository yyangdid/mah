package mah.common.json.support.fastjson;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import mah.common.json.JSONArr;
import mah.common.json.JSONException;
import mah.common.json.JSONObj;

import java.util.AbstractMap;
import java.util.Date;
import java.util.Set;

/**
 * Created by zgq on 16-12-4.
 */
public class FastjsonAdapter extends AbstractMap<String,Object> implements JSONObj {

    private JSONObject jsonObject;

    public FastjsonAdapter(JSONObject jsonObject) {
        if (jsonObject == null) {
            throw new JSONException("jsonObject is null!");
        }
        this.jsonObject = jsonObject;
    }
    @Override
    public String getString(String key) {
        return jsonObject.getString(key);
    }

    @Override
    public JSONObj getJSONObj(String key) {
        JSONObject o = this.jsonObject.getJSONObject(key);
        if (o == null) {
            return null;
        }
        return new FastjsonAdapter(o);
    }

    @Override
    public JSONArr getJSONArr(String key) {
        JSONArray arr = this.jsonObject.getJSONArray(key);
        if (arr == null) {
            return null;
        }
        return new FastjsonArrAdapter(arr);
    }

    @Override
    public Date getDate(String key) {
        return this.jsonObject.getDate(key);
    }

    @Override
    public int getInt(String key) {
        return this.jsonObject.getIntValue(key);
    }

    @Override
    public Object put(String key, Object value) {
        return jsonObject.put(key, value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return new FastjsonEntrySet(jsonObject.entrySet());
    }

}
