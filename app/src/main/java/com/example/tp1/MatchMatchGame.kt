package com.example.tp1

import WikipediaAPI
import WikipediaPage
import android.app.ActionBar.LayoutParams
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable

class MatchMatchGame : AppCompatActivity(), OnClickListener, OnGameEventListener
{
    private lateinit var layout: LinearLayout;

    private var idQuitGame: Int = -1;
    private var idEasy: Int = -1;
    private var idMedium: Int = -1;

    private var idTextHealth: Int = -1;
    private var idGameView: Int = -1;

    private var maxHealth: Int = 20;
    private var currHealth: Int = 20;
    private var currDifficulty : Boolean = true;

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()

        layout = LinearLayout(this);
        layout.orientation = LinearLayout.VERTICAL;

        //cree le bouton pour quiter la game
        var button = Button(this)
        button.id = View.generateViewId()
        button.text = "Quit game"
        button.setOnClickListener(this)
        layout.addView(button)
        idQuitGame = button.id

        EndGame()
    }

    override fun onStop() {
        super.onStop()
        layout.removeAllViews()
    }

    override fun onClick(p0: View?) {
        if(p0?.id == idQuitGame)
        {
            val intent = Intent(this, ListPionniers::class.java)
            startActivity(intent)
        }
        if(p0?.id == idEasy)
        {
            StartGame(true)
        }
        if(p0?.id == idMedium)
        {
            StartGame(false)
        }
    }

    fun StartGame(easy: Boolean)
    {
        //efface l'inteface non necessaire
        if(idEasy != -1)
        {
            layout.removeView(findViewById(idEasy))
            idEasy = -1
        }
        if(idMedium != -1)
        {
            layout.removeView(findViewById(idMedium))
            idMedium = -1
        }

        currDifficulty = easy

        //ajoute la view selon la difficulté
        var gameView : GameView
        if(easy)
        {
            gameView = GameView(this,this,4, 4, 200, 200, 1, 5)
        }
        else
        {
            gameView = GameView(this,this,4, 4, 200, 200, 1, 10)
        }

        //initialise le pointage
        var textView = TextView(this)
        textView.id = View.generateViewId()
        currHealth = maxHealth
        textView.text = currHealth.toString()
        textView.textSize = 50f
        idTextHealth = textView.id
        layout.addView(textView)

        gameView.id = View.generateViewId()
        layout.addView(gameView)
        idGameView = gameView.id

        setContentView(layout)
    }

    fun EndGame()
    {
        //enelve l'interface non necessaire
        if(idTextHealth != -1)
        {
            layout.removeView(findViewById(idTextHealth))
            idTextHealth = -1
        }
        if(idGameView != -1)
        {
            layout.removeView(findViewById(idGameView))
            idGameView = -1
        }

        //ajoute un bouton pour jouer en easy
        var buttonEasy = Button(this)
        buttonEasy.id = View.generateViewId()
        buttonEasy.text = "Play easy"
        buttonEasy.setOnClickListener(this)
        layout.addView(buttonEasy)
        idEasy = buttonEasy.id

        //ajoute un bouton pour jouer en medium
        var buttonMedium = Button(this)
        buttonMedium.id = View.generateViewId()
        buttonMedium.text = "Play medium"
        buttonMedium.setOnClickListener(this)
        layout.addView(buttonMedium)
        idMedium = buttonMedium.id

        setContentView(layout)
    }

    //increment le nombre de pionnier dans les sharedPreferences
    fun AddPionieer()
    {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        var pionieerUnlock : Int = sharedPreferences.getInt("pionieerUnlock", -1)

        val editor = sharedPreferences.edit()

        if(pionieerUnlock == -1)
        {
            pionieerUnlock = 0;
            editor.putInt("pionieerUnlock", pionieerUnlock);
            editor.apply();
        }

        pionieerUnlock += 1;
        editor.putInt("pionieerUnlock", pionieerUnlock);
        editor.apply();
    }

    //eface la sauvegarde de pionieer
    fun ErasePioneers()
    {
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        var pionieerUnlock : Int = sharedPreferences.getInt("pionieerUnlock", -1)

        val editor = sharedPreferences.edit()

        pionieerUnlock = 0;
        editor.putInt("pionieerUnlock", pionieerUnlock);
        editor.apply();
    }

    //fonction appeler lorsque toute les case sont retourner
    override fun OnWinGame()
    {
        AddPionieer()
        DialogueWin()
        EndGame()
    }

    //fonction appeler lorsque la valeur des deux case n'est pas pareil
    override fun OnLosePoint()
    {
        currHealth -= 1
        findViewById<TextView>(idTextHealth).text = currHealth.toString()
        if(currHealth == 0)
        {
            EndGame()
            DialogueLost()
        }
    }

    //dialogue quand le joueur pert
    fun DialogueLost() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Lost")
        builder.setMessage("Voulez vous rejouer")

        builder.setIcon(R.drawable.image_lost)


        builder.setPositiveButton("Accepter") { dialog, which ->
            StartGame(currDifficulty)
        }


        builder.setNegativeButton("Décliner") { dialog, which ->

        }


        val dialog = builder.create()
        dialog.show()
    }

    //diaglogue quand le joueur gagne
    fun DialogueWin() {
        //va chercher le pionnier debloquer
        val sharedPreferences = getSharedPreferences("data", Context.MODE_PRIVATE)
        var pionieerUnlock : Int = sharedPreferences.getInt("pionieerUnlock", -1)

        val api = WikipediaAPI()
        val listPionniers : List<String> = ReadJsonFile(this, "pionniers.json")

        val context = this

        //va faire la requete a l'API
        if(pionieerUnlock <= listPionniers.size) {
            api.GetPageInfo(listPionniers[pionieerUnlock - 1], object : WikipediaAPI.OnPageInfoReceivedListener {
                override fun onPageInfoReceived(page: WikipediaPage?) {
                    //revien sur le thread du UI
                    runOnUiThread {
                        //affiche le dialogue avec les info
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Win")
                        builder.setMessage("Vous avez débloquer " + listPionniers[pionieerUnlock - 1])

                        // Récupération de votre image bitmap
                        val bitmap: Bitmap = page?.image!!

                        // Conversion de votre image bitmap en un BitmapDrawable
                        val bitmapDrawable = BitmapDrawable(resources, bitmap)

                        // Ajout du BitmapDrawable à la boîte de dialogue
                        builder.setIcon(bitmapDrawable)

                        builder.setPositiveButton("Accepter") { dialog, which ->

                        }

                        val dialog = builder.create()
                        dialog.show()
                    }
                }
            })
        }
    }
}