package net.takoko.htmlparser;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText takoSitio;
    private TextView enlaces;
    private Button botonObtener;
    private SpannableString spString;

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
                    Elements enlaces = documento.select("p");//div[class=entry-content]


                    builder.append(titulo).append("\n\n");

                    for (Element enlace : enlaces){
                        if (enlace.text().equals("Quiero recibir noticias en mi email")) break;
                        if (enlace.text().isEmpty()) continue;
                        List<Node> nodos = enlace.childNodes();

                        for (Node nodo: nodos){
                            String textoTemporal = quitarEtiqueta(nodo.toString(), nodo.nodeName());
                            String espaciado;
                            if (textoTemporal.endsWith(".") || nodo.outerHtml().endsWith(".")) espaciado = "\n\n";
                            else espaciado = "";

                            switch(nodo.nodeName()){
                                case "strong":
                                    spString = new SpannableString(textoTemporal);
                                    int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                                    spString.setSpan(new StyleSpan(Typeface.BOLD), 0, spString.length(), flag);
                                    builder.append(spString).append(espaciado);
                                    break;
                                case "em":
                                    spString = new SpannableString(textoTemporal);
                                    flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                                    spString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spString.length(), flag);
                                    builder.append(spString).append(espaciado);
                                    break;
                                case "span":
                                    break;
                                default:
                                    spString = new SpannableString(textoTemporal);
                                    builder.append(spString).append(espaciado);
                                    break;
                                    //builder.append("\n").append(nodo.outerHtml()).append("\n");
                            }
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


}
