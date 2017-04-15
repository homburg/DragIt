package dk.jfjf.dragit

import android.content.ClipData
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

data class Point(val x: Int, val y: Int)

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var disposable: CompositeDisposable? = null

    private val targetView: View by lazy { findViewById(R.id.text) }
    private val wrapView: TextView by lazy { findViewById(R.id.wrapText) as TextView }
    private val containerView: View by lazy { findViewById(R.id.main_content_layout) }

    private val textViewDrags: Observable<DragEvent> by lazy {
        RxView.drags(containerView)
    }

    private val coords: Observable<Point> by lazy {
        textViewDrags
                .filter { it.action == DragEvent.ACTION_DRAG_LOCATION }
                .map { Point(it.x.toInt(), it.y.toInt()) }
                .map { centerOnTarget(targetView, it) }
    }

    private val dragEventMap: Map<Int, String> = mapOf(
            DragEvent.ACTION_DRAG_ENDED to "ENDED",
            DragEvent.ACTION_DRAG_ENTERED to "ENTERED",
            DragEvent.ACTION_DRAG_EXITED to "EXITED",
            DragEvent.ACTION_DRAG_STARTED to "STARTED",
            DragEvent.ACTION_DRAG_LOCATION to "LOCATION"
    )

    private fun dragEventName(event: DragEvent): String {
        return dragEventMap.getOrDefault(event.action, "UNKNOWN")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)

        disposable = CompositeDisposable()

        setupTouch(targetView)

        subscribeForDrags()

        wrapView.text = "some text or what ever".split(" ").joinToString(separator = "\u00a0> ")
    }

    private fun subscribeForDrags() {
        coords
                .map(Point::x)
                .filter {
                    it > 0
                }
                .subscribe {
                    Timber.d("offset %d", it)
                    viewWidth(wrapView, it)
                }
    }

    private fun setupTouch(view: View) {
        RxView.touches(view)
                .subscribe { event: MotionEvent ->
                    Timber.d("Touched! %s", event.action)
                    startDrag(view)
                }
    }

    override fun onDestroy() {
        super.onDestroy()

        disposable!!.dispose()
    }

    private fun startDrag(view: View) {
        Timber.d("Start drag %s", view)
        val clipData = ClipData.newPlainText("clip label", "clip text")
        val shadowBuilder = View.DragShadowBuilder()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            view.startDragAndDrop(clipData, shadowBuilder, null, 0)
        } else {
            view.startDrag(clipData, shadowBuilder, null, 0)
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    companion object {
        private fun centerOnTarget(target: View, p: Point): Point {
            val width = target.width
            val height = target.height
            val x = p.x - width / 2
            val y = p.y - height / 2
            return Point(x, y)
        }

        private fun centerX(target: View, p: Point): Point {
            val width = target.width
            val x = p.x - width / 2
            return Point(x, p.y)
        }

        private fun centerY(target: View, p: Point): Point {
            val width = target.width
            val x = p.x - width / 2
            return Point(x, p.y)
        }

        private fun placeView(target: View, p: Point) {
            val layoutParams = RelativeLayout.LayoutParams(target.layoutParams)
            layoutParams.leftMargin = p.x
            layoutParams.topMargin = p.y
            target.layoutParams = layoutParams
        }

        private fun viewWidth(target: View, width: Int) {
            target.layoutParams = LinearLayout.LayoutParams(width, target.layoutParams.height)
        }
    }
}
