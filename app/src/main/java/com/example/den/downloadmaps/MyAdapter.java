package com.example.den.downloadmaps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.view.View.INVISIBLE;

public class MyAdapter extends ArrayAdapter<Place> {

    private LayoutInflater inflater; // для загрузки разметки элемента
    private int layout;              // идентфикатор файла разметки
    private List<Place> place;
    private Context context;
    private String urlFirst = "http://download.osmand.net/";                     // "https://futurestud.io/"
    private String urlSecond = "download.php?standard=yes&file=Denmark_europe_2.obf.zip";   // "images/futurestudio-university-logo.png"

    public MyAdapter(Context context, int resource, List<Place> place) {
        super(context, resource, place);

        this.inflater = LayoutInflater.from(context);
        this.layout = resource;
        this.place = place;
        this.context = context;
    }

    private class ViewHolder {
        final ImageView image;
        final TextView text;
        final ImageView download;
        final View line;

        public ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.imageView);
            text = (TextView) view.findViewById(R.id.textViewName);
            download = (ImageView) view.findViewById(R.id.imageClick);
            line = (View) view.findViewById(R.id.lines);
        }
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder; //объект класса ViewHolder

        if (convertView == null) {
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new MyAdapter.ViewHolder(convertView); // создать viewholder
            convertView.setTag(viewHolder);  // сохранить ссылки на элементы разметки
        } else {
            // читаем сохраненный элемент
            viewHolder = (MyAdapter.ViewHolder) convertView.getTag();
        } // if

        // связать отображаемые элементы и значения полей
        if (place.get(position).getDepth() == 2) {
            viewHolder.image.setImageResource(R.mipmap.ic_world_globe_dark);
        } else viewHolder.image.setImageResource(R.mipmap.ic_map);

        viewHolder.text.setText(place.get(position).getName());

        if (place.get(position).isMap() == true) {
            viewHolder.download.setImageResource(R.mipmap.ic_action_import);
        }else viewHolder.download.setVisibility(INVISIBLE);

        //Слушатель для загрузки
        View.OnClickListener listnerDownload = new View.OnClickListener() {
            @SuppressLint("ShowToast")
            @Override
            public void onClick(View v) {
                if (InternetConnection.checkConnection(context)) {
                    downloadFile();
                } else {
                    Toast.makeText(context, R.string.string_internet_connection_not_available, Toast.LENGTH_LONG);
                }
            }
        };
        // назначить слушателя события - клик по кнопке элемента
        viewHolder.download.setOnClickListener(listnerDownload);
        if (position == place.size() - 1) viewHolder.line.setVisibility(INVISIBLE);
        // вернуть ссылку на сформированный элемент интерфейса
        return convertView;
    } // getView

    private void downloadFile() {
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(urlFirst);//https://futurestud.io/images/futurestudio-university-logo.png
        Retrofit retrofit = builder.build();

        ApiServiceMap apiServiceMap = retrofit.create(ApiServiceMap.class);
        retrofit2.Call<ResponseBody> call = apiServiceMap.downloadFileWithDynamicUrlSync(urlSecond);


        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, final Response<ResponseBody> response) {
                task(response);
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
            }
        });
    }

    private boolean writeResponseBodyToDisk(ResponseBody body) {
        try {
            File futureStudioIconFile = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "file.obf.zip");//Denmark_europe_2.obf.zip

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    100);
        }
    }//checkPermission

    @SuppressLint("StaticFieldLeak")
    private void task(final Response<ResponseBody> response) {
        checkPermission();
        final Dialog dialog = new Dialog(context);


        new AsyncTask<Void, Void, Void>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                if (response.isSuccessful()) {
                    dialog.setTitle("Ожидайте, начато скачивание!!!");
                } else {
                    dialog.setTitle("нет Респонзе");
                }
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                writeResponseBodyToDisk(response.body());
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                dialog.cancel();
                Dialog dialog1 = new Dialog(context);
                dialog1.setTitle("Скачано");
                dialog1.show();
            }
        }.execute();
    }
}
