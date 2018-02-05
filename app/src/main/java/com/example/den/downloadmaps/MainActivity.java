package com.example.den.downloadmaps;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    final String TAG = "МойЛог";
    private ArrayMap<Place, String> newList = new ArrayMap<>();  //все элементы ХМL файла
    private MyAdapter adapter;
    private ListView listView;
    private ArrayList<Place> list;                             //список для адаптера
    private String parentName = "continent";
    private ArrayList<String> pathParents = new ArrayList<>();  //список - путь
    private boolean flagAddParent = true;                       //флаг для возврата на уровень выше
    private String fileName = "regions.xml";                     //имя XML файла
    private LinkedHashSet<String> withDepth = new LinkedHashSet<>();
    private Toolbar toolbar;
    private Resources res;//доступ к ресурсам


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        res = getResources();

        XmlPullParser parser = parseXML();
        createArrayMap(parser, true);
        parser = parseXML();
        createArrayMap(parser, false);
        list = createList(parentName);

        listView = findViewById(R.id.listView);
        adapter = new MyAdapter(this, R.layout.adapter_for_list, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (checkParent(list.get(position).getName())) {
                    parentName = list.get(position).getName();
                    toolbar.setTitle(parentName);
                    list = createList(parentName);
                    adapter = new MyAdapter(MainActivity.this, R.layout.adapter_for_list, list);
                    listView.setAdapter(adapter);
                }
            }
        });
        ViewCompat.setNestedScrollingEnabled(listView, true);
    }//onCreate

    private XmlPullParser parseXML() {
        XmlPullParser parser = null;
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            parser = parserFactory.newPullParser();
            InputStream is = getAssets().open(fileName);
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return parser;
    }//parseXML

    private String createNameToList(XmlPullParser parser) {
        String valueAttributeTranslate = parser.getAttributeValue(null, "translate");
        String valueAttributeName = parser.getAttributeValue(null, "name");
        String place = "";

        if (valueAttributeTranslate != null) {
            if (!Character.isUpperCase(valueAttributeTranslate.charAt(0))) {//если первая буква маленькая
                for (int j = 0; j < valueAttributeTranslate.length(); j++) {
                    if (Character.isUpperCase(valueAttributeTranslate.charAt(j))) {
                        place = valueAttributeTranslate.substring(j);
                        break;
                    }//if
                }//for
            } else place = valueAttributeTranslate;
            int i = place.indexOf(';');//ищем в переводе
            place = (i != -1) ? place.substring(0, i) : place;
        } else if (valueAttributeName != null) {
            place = valueAttributeName.substring(0, 1).toUpperCase() + valueAttributeName.substring(1);//Первая буква заглавная
        }//if
        return place;
    }//createNameToList

    private void helpHM(String curName, List<String> lastName, int getDepth) {
        boolean maps = true;
        Iterator<String> iterVal = withDepth.iterator();
        for (int i = 0; i < withDepth.size(); i++) {
            String nameWithDepth = iterVal.next();
            if (curName.equals(nameWithDepth)) {
                maps = false;
            }
        }
        Place place = new Place(lastName.get(lastName.size() - 1), getDepth, maps);//cоздаем объект региона
        newList.put(place, lastName.get(lastName.size() - 2));//добавляем запись ("имя", "имя родителя")
    }//helpHM

    private void createArrayMap(XmlPullParser parser, boolean help) {
        List<String> lastName = new ArrayList<>();
        lastName.add(parentName);

        try {
            while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                String curName = parser.getAttributeValue(null, "name");//атрибут "name" в теге
                if (curName != null && lastName.size() > 0 && !createNameToList(parser).equals(lastName.get(lastName.size() - 1))) {

                    if (lastName.size() + 1 < parser.getDepth()) {//если уровень стал глубже
                        curName = createNameToList(parser);//приводим в читабельный вид
                        lastName.add(curName);
                    } else if (lastName.size() + 1 > parser.getDepth()) {//если глубина уменьшилась
                        curName = createNameToList(parser);//приводим в читабельный вид
                        while ((lastName.size() + 1) - parser.getDepth() > 1) {
                            lastName.remove(lastName.size() - 1);
                        }
                        lastName.set(lastName.size() - 1, curName);//переписываем последнее имя
                        if (help) {// парсим для единственного экземпляра или для создания общего списка
                            withDepth.add(lastName.get(lastName.size() - 2));//добавляем элемент с вложением
                        } else helpHM(curName, lastName, parser.getDepth());
                    } else if (lastName.size() + 1 == parser.getDepth()) {//если на том же уровне
                        curName = createNameToList(parser);//приводим в читабельный вид
                        lastName.add(curName);//добавляем в список
                        if (help) {// парсим для единственного экземпляра или для создания общего списка
                            withDepth.add(lastName.get(lastName.size() - 2));//добавляем элемент с вложением
                        } else helpHM(curName, lastName, parser.getDepth());
                    }//if
                }//if
                parser.next();
            }//wile
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }//try-catch
    }//createArrayMap

    private ArrayList<Place> createList(String parent) {
        if (flagAddParent) {
            pathParents.add(parent);
        }//if
        flagAddParent = true;
        ArrayList<Place> list = new ArrayList<>();
        Set<Place> set = newList.keySet();
        for (Place place : set) {

            if (newList.get(place).equals(parent)) {
                if (!place.getName().isEmpty())
                    list.add(place);
            }//if
        }//for
        Collections.sort(list, Place.COMPARE_BY_NAME);
        return list;
    }//createList

    private Boolean checkParent(String parent) {
        boolean flag = false;
        Set<Place> set = newList.keySet();
        for (Place place : set) {

            if (newList.get(place).equals(parent)) {
                if (!place.getName().isEmpty())
                    flag = true;
            }//if
        }//for
        return flag;
    }//checkParent

    @Override
    public void onBackPressed() {
        if (!parentName.equals("continent")) {
            int n = pathParents.size() - 2;
            parentName = pathParents.get(n);

            if (parentName.equals("continent")) {
                toolbar.setTitle(res.getString(R.string.app_name));
            } else toolbar.setTitle(parentName);

            pathParents.remove(n + 1);
            flagAddParent = false;
            list = createList(parentName);

            adapter = new MyAdapter(MainActivity.this, R.layout.adapter_for_list, list);
            listView.setAdapter(adapter);

        } else super.onBackPressed();
    }//onBackPressed
}//class MainActivity

