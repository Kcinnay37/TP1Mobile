package com.example.tp1

import WikipediaAPI
import WikipediaPage
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ShowInformation : AppCompatActivity(), OnClickListener
{
    private lateinit var layout : LinearLayout;

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        //va chercher qu'elle est le pionnier a afficher
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        val currPionieer : Int = sharedPreferences.getInt("currPionieer", -1)

        if(currPionieer == -1)
        {
            return;
        }

        //set l'interface --------------------------------------------------------------------------
        layout = LinearLayout(this);
        layout.orientation = LinearLayout.VERTICAL;

        //va recuperer la list des pionniers dans le JSON
        val listPionniers : List<String> = ReadJsonFile(this, "pionniers.json")

        val api = WikipediaAPI()

        //cree un scroll view pour mettre l'image et le text
        val scrollView = ScrollView(this);
        val linearLayout = LinearLayout(this);
        linearLayout.orientation = LinearLayout.VERTICAL;
        scrollView.addView(linearLayout);

        //cree l'image
        val imageView = ImageView(this)
        imageView.id = View.generateViewId()
        imageView.setImageResource(R.mipmap.ic_launcher_interogation_foreground)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.layoutParams = LinearLayout.LayoutParams(1000, 1000)
        linearLayout.addView(imageView)

        //cree le text
        val textView = TextView(this)
        textView.id = View.generateViewId()
        textView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearLayout.addView(textView)

        //cree le bouton pour backer
        val buttonBack = Button(this);
        buttonBack.id = View.generateViewId();
        buttonBack.text = "Back";
        buttonBack.setOnClickListener(this);
        layout.addView(buttonBack);

        //va faire une requete avec l'API et defini quoi faire avec la reponse
        api.GetPageInfo(listPionniers[currPionieer], object : WikipediaAPI.OnPageInfoReceivedListener {
            override fun onPageInfoReceived(page: WikipediaPage?) {
                //sur le UI Thread defini les valeur des view
                runOnUiThread {
                    imageView.setImageBitmap(page?.image)
                    textView.text = page?.extract
                }
            }
        })

        layout.addView(scrollView);

        setContentView(layout);

        //------------------------------------------------------------------------------------------
    }

    override fun onStop() {
        super.onStop()
        layout.removeAllViews()
    }

    override fun onClick(p0: View?) {
        val intent = Intent(this, ListPionniers::class.java);
        startActivity(intent);
    }
}