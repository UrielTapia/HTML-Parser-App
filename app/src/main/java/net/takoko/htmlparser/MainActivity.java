package net.takoko.htmlparser;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText takoSitio;
    private TextView enlaces;
    private Button botonObtener;
    private SpannableString spString;
    private Element nodoActual;
    private Boolean esImagen;
    private float width, height;

    public String quitarEtiqueta(String texto, String etiqueta) {
        etiqueta = "<"+etiqueta+">";
        texto = texto.replaceAll(etiqueta, ""); //Se quita etiqueta de apertura
        return texto.replaceAll(new StringBuilder(etiqueta).insert(1, "/").toString(), ""); //Se quita y se devuelve sin etiqueta de cierre
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takoSitio = (EditText) findViewById(R.id.takoSitio);

        enlaces = (TextView) findViewById(R.id.enlaces);
        enlaces.setMovementMethod(new ScrollingMovementMethod());

        botonObtener = (Button) findViewById(R.id.botonObtener);
        botonObtener.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (takoSitio.getText().toString().isEmpty()) takoSitio.setError("Ingresa un enlace de Takoko.");
                else obtenSitioWeb();
            }
        });
    }

    private void obtenSitioWeb(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final SpannableStringBuilder builder = new SpannableStringBuilder();

                try {
                    Document documento = Jsoup.connect(takoSitio.getText().toString()).get();
                    //String titulo = documento.title(); //Incluye "- Takoko" y está sobrado.
                    String titulo = documento.select("h1[class=entry-title]").first().text();
                    Elements entryContent = documento.select("div.entry-content");//p
                    Elements contenido = documento.select("div [class=entry-content] p"); //Se vienen texto e imágenes (:not no funciona)
                    //Elements imagenes = documento.select("div [class=entry-content] p:has(img)"); Sólo trae imágenes, sí funciona, pero es inútil sin texto suelto.
                    Elements header1 = documento.select("div.entry-content h1");
                    Elements header2 = documento.select("div.entry-content h2");
                    Elements header3 = documento.select("div.entry-content h3");
                    Elements header4 = documento.select("div.entry-content h4");
                    Elements header5 = documento.select("div.entry-content h5");

                    Iterator cont = contenido.iterator();
                    Iterator h1 = header1.iterator();
                    Iterator h2 = header2.iterator();
                    Iterator h3 = header3.iterator();
                    Iterator h4 = header4.iterator();
                    Iterator h5 = header5.iterator();

                    spString = new SpannableString(titulo);
                    int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                    spString.setSpan(new StyleSpan(Typeface.BOLD), 0, spString.length(), flag);
                    spString.setSpan(new RelativeSizeSpan(1.5f), 0, spString.length(), flag);
                    builder.append(spString).append("\n\n");

                    List<Node> etiquetas = entryContent.first().childNodes();

                    for (Node etiqueta : etiquetas){
                        //DEBUG: SHOW ETIQUETAS - builder.append("\n\n").append(etiqueta.nodeName());

                        switch(etiqueta.nodeName()){
                            case "#text":
                                break;
                            case "p":
                                if (cont.hasNext()){
                                    nodoActual = (Element) cont.next();
                                    esImagen = (nodoActual.text().isEmpty() && nodoActual.outerHtml().contains("img"))? true: false;
                                    if (esImagen){
                                        spString = new SpannableString("abcde");

                                        Bitmap takoko = getBitmapFromURL(nodoActual.childNode(0).attr("src"));

                                        /*float relacion = fuente.getWidth()/fuente.getHeight();

                                        if (relacion > 1){
                                            width = getWindowManager().getDefaultDisplay().getWidth();
                                            height = width/relacion;
                                        }
                                        if (relacion < 1){
                                            height = getWindowManager().getDefaultDisplay().getHeight()/1.5f;
                                            width = height*relacion;
                                        }
                                        else{
                                            width = getWindowManager().getDefaultDisplay().getWidth();
                                            height = width;
                                        }*/

                                        ImageSpan imageSpan = new ImageSpan(getApplicationContext(), takoko);
                                        spString.setSpan(imageSpan, 0, spString.length(),Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                                        builder.append(spString).append("\n\n");
                                    }
                                    //builder.append(nodoActual.childNode(0).attr("src")+"\n\n");
                                    else
                                        formatoTexto(nodoActual.childNodes(), builder, flag);
                                }
                                break;
                            case "h1":
                                if (cont.hasNext()) {
                                    nodoActual = (Element) h1.next();
                                    formatoHeader(builder, nodoActual.text(), 1.5, flag);
                                }
                                break;
                            case "h2":
                                if (cont.hasNext()) {
                                    nodoActual = (Element) h2.next();
                                    formatoHeader(builder, nodoActual.text(), 1.4, flag);
                                }
                                break;
                            case "h3":
                                if (cont.hasNext()) {
                                    nodoActual = (Element) h3.next();
                                    formatoHeader(builder, nodoActual.text(), 1.3, flag);
                                }
                                break;
                            case "h4":
                                if (cont.hasNext()) {
                                    nodoActual = (Element) h4.next();
                                    formatoHeader(builder, nodoActual.text(), 1.2, flag);
                                }
                                break;
                            case "h5":
                                if (cont.hasNext()) {
                                    nodoActual = (Element) h5.next();
                                    formatoHeader(builder, nodoActual.text(), 1.1, flag);
                                }
                                break;
                            case "div": //Posiblemente video YouTube
                                break;
                            case "ul":  //Listas, no soportadas. Probar.
                                break;
                            case "pre": //Contenedor, no soportado.
                                break;
                            case "default":
                                break;
                        }

                        //    builder/*.append("\n").append("Enlace: ").append(enlace.attr("href"))
                        //            .append("\nPrueba: ").append(enlace.attr("rel"))*/
                        //            .append("\n")/*.append("Texto: ")*/.append(enlace.wholeText).append("\n");
                    }

                } catch (IOException e) {
                    builder.append("Error: ").append(e.getMessage());
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        enlaces.setText(builder);
                    }
                });
            }
        }).start();
    }


    public void formatoTexto (List<Node> nodos, SpannableStringBuilder builder, int flag){
        for (Node nodo: nodos){
            String textoTemporal = quitarEtiqueta(nodo.toString(), nodo.nodeName());
            String espaciado;
            if (textoTemporal.endsWith(".") || nodo.outerHtml().endsWith(".")) espaciado = "\n\n";
            else espaciado = "";

            switch(nodo.nodeName()){
                case "strong":
                    spString = new SpannableString(textoTemporal);
                    spString.setSpan(new StyleSpan(Typeface.BOLD), 0, spString.length(), flag);
                    builder.append(spString).append(espaciado);
                    break;
                case "em":
                    spString = new SpannableString(textoTemporal);
                    spString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spString.length(), flag);
                    builder.append(spString).append(espaciado);
                    break;
                case "span":
                    break;
                default:
                    spString = new SpannableString(textoTemporal); //textoTemporal
                    builder.append(spString).append(espaciado);
                    break;
            }
        }
    }

    public void formatoHeader (SpannableStringBuilder builder, String header, double tamano, int flag){
        String espaciado = "\n\n";
        spString = new SpannableString(header);
        spString.setSpan(new RelativeSizeSpan((float)tamano), 0, spString.length(), flag);
        builder.append(spString).append(espaciado);
    }

    public Drawable loadImageFromURL(String url, String name) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, name);
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}