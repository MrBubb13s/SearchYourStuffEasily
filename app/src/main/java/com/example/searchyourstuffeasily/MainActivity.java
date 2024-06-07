package com.example.searchyourstuffeasily;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.searchyourstuffeasily.ui.dashboard.DashboardFragment;
import com.example.searchyourstuffeasily.ui.home.HomeFragment;
import com.example.searchyourstuffeasily.ui.notifications.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = null;
                switch (menuItem.getItemId()){
                    case R.id.nav_furniture:
                        fragment = new HomeFragment();
                        LinearLayout layout1 = findViewById(R.id.container);
                        layout1.setBackgroundResource(R.drawable.image_furniture);
                        break;
                    case R.id.nav_refrigerator:
                        fragment = new DashboardFragment();
                        LinearLayout layout2 = findViewById(R.id.container);
                        layout2.setBackgroundResource(R.drawable.image_refrigerator);
                        break;
                    case R.id.nav_information:
                        fragment = new NotificationsFragment();
                        LinearLayout layout3 = findViewById(R.id.container);
                        layout3.setBackgroundResource(R.drawable.image_information);
                        break;
                }
                return loadFragment(fragment);
            }
        });
        navView.setSelectedItemId(R.id.nav_furniture);
    }
    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}