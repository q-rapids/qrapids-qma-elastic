package evaluation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import util.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Project{

    /**
     * This method returns the list of suffixes used for storing the different projects assessment. The indexes for an specific project
     * have this suffixes concatenated to the name of the index
     *
     * @return the list of suffixes (strings) corresponding to the strategic indicator indexes
     * @throws IOException
     */
    public static List<String> getProjects() throws IOException {
        JsonParser parser = new JsonParser();
        String index_name, suffix;
        int pos_index, pos_suffix, pos_preffix;
        boolean is_added;

        JsonArray json = parser.parse(convertStreamToString(util.Queries.getIndexes().getEntity().getContent())).getAsJsonArray();
        List<String> result = new ArrayList<>();

        System.err.println("GETPROJECT: Json size: " + json.size());

        for (int i = 0; i < json.size(); ++i) {
            JsonObject object = json.get(i).getAsJsonObject();
            index_name = object.get("index").getAsString();

            is_added = false;
            pos_index = index_name.lastIndexOf(Constants.INDEX_FACTORS);
            if (pos_index != -1) {
                pos_preffix = index_name.lastIndexOf('.', pos_index);
                pos_suffix = index_name.indexOf('.', pos_index);
                if (pos_suffix != -1) {// there is a suffix
                    // check on preffix
                    if ((Constants.INDEX_PREFIX.isEmpty() && pos_preffix == -1) ||
                            !Constants.INDEX_PREFIX.isEmpty() && pos_preffix != -1 &&
                                    Constants.INDEX_PREFIX.substring(0, pos_preffix).equalsIgnoreCase(Constants.INDEX_PREFIX)) {
                        String cmp = index_name.substring(pos_suffix + 1, index_name.length());
                        result.add(cmp);
                        System.err.println("GETPROJECT: index addex: " + index_name + "(project: " + cmp + ")");
                        is_added = true;
                    }
                }
            }
            if (!is_added)
                System.err.println("GETPROJECT: index DON'T addex: " + index_name );
        }
        return result;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
