package com.example.tp1

import WikipediaAPI
import WikipediaPage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.collections.HashMap

class ListPionniers : AppCompatActivity(), View.OnClickListener
{
    private lateinit var layout : LinearLayout;
    private lateinit var listPionniers: List<String>;
    private var pionieerUnlock : Int = -1

    private lateinit var imagePionniers : HashMap<Int, Int>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState);
    }

    override fun onStart() {
        super.onStart()

        //va chercher le nombre de pionnier debloquer
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        pionieerUnlock = sharedPreferences.getInt("pionieerUnlock", -1)

        // initialise l'interface ------------------------------------------------------------------
        layout = LinearLayout(this);
        layout.orientation = LinearLayout.VERTICAL;

        //bouton pour aller jouer au jeu
        val buttonStartGame = Button(this);
        buttonStartGame.id = View.generateViewId();
        buttonStartGame.text = "Start game";
        buttonStartGame.setOnClickListener(this);
        layout.addView(buttonStartGame);

        //va chercher la list des pionniers dans le JSON
        listPionniers = ReadJsonFile(this, "pionniers.json")

        //dictionnaire qui va contenir tout les pionnier et le position dans la list
        imagePionniers = HashMap<Int, Int>()

        //Va permettre de faire les requete avec l'API
        val api = WikipediaAPI()

        //cree un srollview pour mettre tout les pionnier
        val scrollView = ScrollView(this);
        val linearLayout = LinearLayout(this);
        linearLayout.orientation = LinearLayout.VERTICAL;
        scrollView.addView(linearLayout);

        //pour tout les pionniers
        var count : Int = 0;
        for (name in listPionniers) {
            //layout pour alligner l'image et le text
            val horizontalLayout = LinearLayout(this)
            horizontalLayout.orientation = LinearLayout.HORIZONTAL

            //cree l'image
            val imageView = ImageView(this)
            imageView.id = View.generateViewId()
            imageView.setImageResource(R.mipmap.ic_launcher_interogation_foreground)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.layoutParams = LinearLayout.LayoutParams(500, 500)
            horizontalLayout.addView(imageView)
            imagePionniers.put(imageView.id, count);

            //cree le text
            val textView = TextView(this)
            textView.id = View.generateViewId()
            textView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            horizontalLayout.addView(textView)

            //si le pionnier est debloquer
            if(count < pionieerUnlock)
            {
                //met l'image clickable
                imageView.setOnClickListener(this);

                //execute la requete avec l'API et defini la fonctiona appeler lorque la requete est fini
                api.GetPageInfo(name, object : WikipediaAPI.OnPageInfoReceivedListener {
                    override fun onPageInfoReceived(page: WikipediaPage?) {
                        //set les changement a apporter sur le UI thread
                        runOnUiThread {
                            //set l'image
                            imageView.setImageBitmap(page?.image)

                            //set le text
                            if(page?.extract != null && page?.extract.length > 200)
                            {
                                textView.text = page?.extract.substring(0, 200) + "...";
                            }
                            else
                            {
                                textView.text = page?.extract
                            }
                        }
                    }
                })
            }

            linearLayout.addView(horizontalLayout)

            count += 1;
        }

        layout.addView(scrollView);

        setContentView(layout);

        // -----------------------------------------------------------------------------------------
    }

    override fun onStop() {
        super.onStop()

        layout.removeAllViews();
    }

    override fun onClick(p0: View?)
    {
        lateinit var intent: Intent

        if(p0 is Button)
        {
            intent = Intent(this, MatchMatchGame::class.java);
        }
        else if(p0 is ImageView)
        {
            //va dire c'est qu'elle le pionnier actif
            val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val currPionniers : Int = imagePionniers[p0.id] ?: -1;
            editor.putInt("currPionieer", currPionniers);
            editor.apply();

            intent = Intent(this, ShowInformation::class.java);
        }
        startActivity(intent);
    }
}

//va retourner la list de nom du JSON file
fun ReadJsonFile(context: Context, fileName: String): List<String>
{
    //ouvre le fichier json des assets
    val inputStream = context.assets.open(fileName)
    //la list qui va contenir les nom
    val jsonNames = mutableListOf<String>()

    //useLine va permettre d'avoir tout les lignes du fichier
    inputStream.bufferedReader().useLines { lines ->
        //va enlever les ligne qui contien just { ou }
        lines.filterNot { line ->
            line.trim() == "{" || line.trim() == "}"
        //pour tout les ligne restante
        }.forEach { line ->
            //va garder just le nom dans la ligne
            val name = line.substringAfter("\"name\" : \"").substringBefore("\"")
            jsonNames.add(name)
        }
    }

    return jsonNames
}