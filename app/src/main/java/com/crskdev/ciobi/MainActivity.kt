package com.crskdev.ciobi

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.commit
import com.crskdev.ciobi.system.util.checkDeniedPermissions
import com.crskdev.ciobi.system.util.getAccountName
import com.crskdev.ciobi.ui.CiobiFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSIONS_CODE: Int = 1337
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionBar?.subtitle = getAccountName()
        if (savedInstanceState == null)
            supportFragmentManager.commit {
                replace(R.id.container, CiobiFragment())
            }
        checkDeniedPermissions(this).takeIf { it.isNotEmpty() }?.run {
            ActivityCompat.requestPermissions(this@MainActivity, this, PERMISSIONS_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_CODE -> {
                val allGranted =
                    grantResults.fold(true) { acc, curr -> acc && curr == PackageManager.PERMISSION_GRANTED }
//                if (!allGranted) {
//                    Toast.makeText(
//                        this.applicationContext,
//                        getString(R.string.permissions_rejected),
//                        Toast.LENGTH_LONG
//                    ).show()
//                }
                return
            }
        }
    }

}
