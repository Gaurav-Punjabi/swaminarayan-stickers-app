/*
 * Copyright (c) WhatsApp Inc. and its affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree.
 */

package org.bhujmandir.swaminarayan.stickers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class EntryActivity extends BaseActivity {
      private View progressBar;
      private LoadListAsyncTask loadListAsyncTask;

      @Override
      protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Fresco.initialize(this);
            setContentView(R.layout.activity_entry);
            overridePendingTransition(0, 0);
            if (getSupportActionBar() != null) {
                  getSupportActionBar().hide();
            }
            Intent intent = new Intent(this.getApplicationContext(), StickerPackListActivity.class);

            progressBar = findViewById(R.id.entry_activity_progress);
            loadListAsyncTask = new LoadListAsyncTask(this);
            loadListAsyncTask.execute();
      }

      private void showStickerPack(ArrayList<StickerPack> stickerPackList) {
            progressBar.setVisibility(View.GONE);
            if (stickerPackList.size() > 1) {
                  final Intent intent = new Intent(this, StickerPackListActivity.class);
                  intent.putParcelableArrayListExtra(StickerPackListActivity.EXTRA_STICKER_PACK_LIST_DATA, stickerPackList);
                  startActivity(intent);
                  finish();
                  overridePendingTransition(0, 0);
            } else {
                  final Intent intent = new Intent(this, StickerPackDetailsActivity.class);
                  intent.putExtra(StickerPackDetailsActivity.EXTRA_SHOW_UP_BUTTON, false);
                  intent.putExtra(StickerPackDetailsActivity.EXTRA_STICKER_PACK_DATA, stickerPackList.get(0));
                  startActivity(intent);
                  finish();
                  overridePendingTransition(0, 0);
            }
      }

      private void showErrorMessage(String errorMessage) {
            progressBar.setVisibility(View.GONE);
            Log.e("EntryActivity", "error fetching stickers packs, " + errorMessage);
            final TextView errorMessageTV = findViewById(R.id.error_message);
            errorMessageTV.setText(getString(R.string.error_message, errorMessage));
      }

      @Override
      protected void onDestroy() {
            super.onDestroy();
            if (loadListAsyncTask != null && !loadListAsyncTask.isCancelled()) {
                  loadListAsyncTask.cancel(true);
            }
      }

      static class LoadListAsyncTask extends AsyncTask<Void, Void, Pair<String, ArrayList<StickerPack>>> {
            private final WeakReference<EntryActivity> contextWeakReference;

            LoadListAsyncTask(EntryActivity activity) {
                  this.contextWeakReference = new WeakReference<>(activity);
            }

            @Override
            protected Pair<String, ArrayList<StickerPack>> doInBackground(Void... voids) {
                  ArrayList<StickerPack> stickerPackList;
                  try {
                        final Context context = contextWeakReference.get();
                        if (context != null) {
                              stickerPackList = StickerPackLoader.fetchStickerPacks(context);
                              if (stickerPackList.size() == 0) {
                                    return new Pair<>("could not find any packs", null);
                              }
                              for (StickerPack stickerPack : stickerPackList) {
                                    StickerPackValidator.verifyStickerPackValidity(context, stickerPack);
                              }
                              return new Pair<>(null, stickerPackList);
                        } else {
                              return new Pair<>("could not fetch stickers packs", null);
                        }
                  } catch (Exception e) {
                        Log.e("EntryActivity", "error fetching stickers packs", e);
                        return new Pair<>(e.getMessage(), null);
                  }
            }

            @Override
            protected void onPostExecute(Pair<String, ArrayList<StickerPack>> stringListPair) {

                  final EntryActivity entryActivity = contextWeakReference.get();
                  if (entryActivity != null) {
                        if (stringListPair.first != null) {
                              entryActivity.showErrorMessage(stringListPair.first);
                        } else {
                              entryActivity.showStickerPack(stringListPair.second);
                        }
                  }
            }
      }
      @Override
      public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_main, menu);

            // return true so that the menu pop up is opened
            return true;
      }
      @Override
      public boolean onOptionsItemSelected(MenuItem item) {
            switch(item.getItemId())
            {
                  case R.id.share_item:
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "To get latest stickers. Download Swaminarayan Sticker for WhatsApp now from Play Store! \n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\nAlso available for iOS Users. ");
                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
                        break;
                  case R.id.more_apps_item:
                        String url = "https://play.google.com/store/apps/developer?id=Shree%20Swaminarayan%20Temple%20Bhuj&hl=en";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        break;

            }
            return true;
      }
}
