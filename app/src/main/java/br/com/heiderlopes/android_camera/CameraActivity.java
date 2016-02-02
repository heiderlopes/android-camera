package br.com.heiderlopes.android_camera;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CameraActivity extends Activity {

    // Activity Request Codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Nome do diretório que serão gravadas as imagens  e os vídeos
    private static final String IMAGE_DIRECTORY_NAME = "appcamera";

    // URL de armazenamento do arquivo(imagem/video)
    private Uri fileUri;

    private ImageView ivPreview;
    private VideoView videoPreview;
    private Button btTirarFoto, btGravarVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ivPreview = (ImageView) findViewById(R.id.ivPreview);
        videoPreview = (VideoView) findViewById(R.id.videoPreview);
        btTirarFoto = (Button) findViewById(R.id.btTirarFoto);
        btGravarVideo = (Button) findViewById(R.id.btGravarVideo);

        //Evento do botão para tirar foto
        btTirarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tirarFoto();
            }
        });

        //Evento do botão para gravar video
        btGravarVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gravarVideo();
            }
        });

        // Checking camera availability
        if (!aparelhoTemSuporteACamera()) {
            Toast.makeText(getApplicationContext(),
                    "O aparelho não possui câmera",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private boolean aparelhoTemSuporteACamera() {
        if (getApplicationContext().getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            //se tiver camera retorna true
            return true;
        } else {
            //se não tiver camera retorna false
            return false;
        }
    }

    //Abrir a camera para tirar a foto
    private void tirarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // Inicia a Intent para tirar a foto
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    /**
     * Here we store the file url as it will be null after returning from camera app
     */
    //
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // salva a url do arquivo caso seja alterada a orientacao da tela
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // recupera o arquivo caso seja alterada a orientação da tela
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    private void gravarVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
        // set video quality
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        //configura o nome do arquivo
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // Inicia a intent para gravar video
        startActivityForResult(intent, CAMERA_CAPTURE_VIDEO_REQUEST_CODE);
    }

    //Chamado depois que a camera é fechada
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Verifica se o resultado é referente a a chamada para tirar foto
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "Cancelada a captura pelo usuário", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Não foi possível tirar a foto", Toast.LENGTH_SHORT)
                        .show();
            }
        } // Verifica se o resultado é referente a chamadda para gravar vídeo
            else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(),
                        "Cancelada a gravação do vídeo pelo usuário", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Não foi possivel gravar o video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    //exibe a imagem tirada
    private void previewCapturedImage() {
        try {
            // hide video preview
            videoPreview.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            BitmapFactory.Options options = new BitmapFactory.Options();
            // Redimensionamento da imagem para não lançar exceção OutOfMemory para imagens muito grande
            options.inSampleSize = 8;
            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);
            ivPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //exibe o video gravado
    private void previewVideo() {
        try {
            // hide image preview
            ivPreview.setVisibility(View.GONE);
            videoPreview.setVisibility(View.VISIBLE);
            videoPreview.setVideoPath(fileUri.getPath());
            //Inicia o video se clicar sobre ele
            videoPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPreview.start();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Cria o arquivo (imagem/video)
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    //retorna a imagem ou o video
    private static File getOutputMediaFile(int type) {
        //Caminho onde será gravado o arquivo
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Cria o diretório caso não exista
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Não foi possível criar o arquivo"
                        + IMAGE_DIRECTORY_NAME);
                return null;
            }
        }
        // Cria o arquivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }
}
