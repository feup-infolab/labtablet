package pt.up.fe.beta.labtablet.utils;

import android.content.Context;
import android.os.Environment;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import pt.up.fe.beta.R;
import pt.up.fe.beta.labtablet.db_handlers.FavoriteMgr;
import pt.up.fe.beta.labtablet.models.DataItem;
import pt.up.fe.beta.labtablet.models.Descriptor;
import pt.up.fe.beta.labtablet.models.FavoriteItem;
import pt.up.fe.beta.labtablet.models.FormInstance;

/**
 * Handles different events when exporting records to csv files
 */
public class CSVHandler {

    public static boolean generateCSV(Context context,
                                      ArrayList<FormInstance> instances,
                                      String favoriteName) throws IOException {

        HashMap<String, ArrayList<FormInstance>> groupedForms = new HashMap<>();

        for (FormInstance formInstance : instances) {
            if (groupedForms.containsKey(formInstance.getParent())) {
                groupedForms.get(formInstance.getParent()).add(formInstance);
                continue;
            }


            ArrayList<FormInstance> forminstaces = new ArrayList<>();
            forminstaces.add(formInstance);
            groupedForms.put(formInstance.getParent(), forminstaces);
        }
        String basePath = Environment.getExternalStorageDirectory() + File.separator
                + context.getString(R.string.app_name) + File.separator
                + favoriteName + File.separator;

        Set<String> entrySet = groupedForms.keySet();
        for (String entryName : entrySet) {

            ArrayList<FormInstance> entryForms = groupedForms.get(entryName);
            CSVWriter writer = new CSVWriter(new FileWriter(basePath + entryName + ".csv"), ',');

            writer.writeNext((String[]) entryForms.get(0).getFormQuestions().toArray());
            for (FormInstance form : entryForms) {
                String[] line = new String[form.getFormQuestions().size()];

                for (int i = 0; i < line.length; ++i) {
                    line[i] = form.getFormQuestions().get(i).getValue();
                }
                writer.writeNext(line);
            }
            writer.close();

            //Add data resource to this favorite
            DataItem csvItem = new DataItem();


            //Build metadata for this file
            ArrayList<Descriptor> itemLevelMetadata = new ArrayList<>();

            ArrayList<Descriptor> loadedDescriptors =
                    FavoriteMgr.getBaseDescriptors(context);

            //If additional metadata is available, it should me added here
            for (Descriptor desc : loadedDescriptors) {
                String tag = desc.getTag();
                switch (tag) {
                    case Utils.TITLE_TAG:
                        desc.setValue(entryName + ".csv");
                        itemLevelMetadata.add(desc);
                        break;
                    case Utils.CREATED_TAG:
                        desc.setValue("" + new Date());
                        itemLevelMetadata.add(desc);
                        break;
                    case Utils.DESCRIPTION_TAG:
                        desc.setValue("");
                        itemLevelMetadata.add(desc);
                        break;
                }
            }

            csvItem.setFileLevelMetadata(itemLevelMetadata);
            csvItem.setDescription("Exported on " + Utils.getDate());
            csvItem.setLocalPath(basePath + entryName + ".csv");

            FavoriteItem favorite = FavoriteMgr.getFavorite(context, favoriteName);
            if (!favorite.getDataItems().contains(csvItem)) {
                favorite.addDataItem(csvItem);
            }
            FavoriteMgr.updateFavoriteEntry(favoriteName, favorite, context);
        }

        return true;
    }
}
