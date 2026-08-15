package fr.archipel.industry;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Coquille WebView : charge le jeu (asset HTML autonome) hors-ligne.
 * Aucune connexion réseau requise pour jouer — tout (React, logique, styles) est inline.
 *
 * Mise à jour quasi-automatique : le jeu (asset de confiance) peut appeler
 * window.ArchipelNative.update(url) pour télécharger l'APK puis lancer l'installeur système
 * via PackageInstaller. L'utilisateur n'a plus qu'à confirmer « Installer » (Android interdit
 * l'installation 100 % silencieuse pour une app sideloadée).
 */
public class MainActivity extends Activity {

    private static final String INSTALL_ACTION = "fr.archipel.industry.INSTALL_STATUS";
    private static final int FILE_CHOOSER_REQUEST = 0xF11E;

    private WebView web;
    private BroadcastReceiver installReceiver;
    // Import de sauvegarde : callback du <input type="file"> en attente du fichier choisi.
    private ValueCallback<Uri[]> filePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);                 // sauvegardes localStorage persistantes
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setTextZoom(100);                           // ignore le zoom système, garde la mise en page

        // Le jeu reste dans la WebView ; les liens http(s) externes (ex. lien de
        // téléchargement d'une mise à jour) s'ouvrent dans le navigateur système.
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return openExternally(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return openExternally(Uri.parse(url));
            }
        });
        web.setBackgroundColor(0xFF0E1726);           // fond sombre pendant le chargement

        // Import de sauvegarde : un <input type="file"> dans le jeu déclenche onShowFileChooser,
        // qui ouvre le sélecteur de fichiers système et renvoie l'URI choisi à la WebView.
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (filePathCallback != null) {
                    filePathCallback.onReceiveValue(null);
                }
                filePathCallback = callback;
                try {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES,
                            new String[]{"text/plain", "application/json"});
                    startActivityForResult(Intent.createChooser(intent, "Choisir une sauvegarde"),
                            FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception e) {
                    filePathCallback = null;
                    return false;
                }
            }
        });

        // Pont natif : réservé à l'asset local de confiance (les URLs externes ne sont JAMAIS
        // chargées dans la WebView, elles partent vers le navigateur système).
        web.addJavascriptInterface(new WebBridge(), "ArchipelNative");
        // Variante MAGASIN (-PstoreBuild=true) : pas d'auto-mise à jour, donc aucun récepteur
        // d'installation à enregistrer. Le reste du pont (export de sauvegarde, sélecteur de
        // fichier, ouverture des liens externes) est INCHANGÉ.
        if (BuildConfig.SELF_UPDATE) registerInstallReceiver();

        setContentView(web);
        applyInsetPadding();
        web.loadUrl("file:///android_asset/index.html");
    }

    /**
     * targetSdk 36 : l'edge-to-edge est IMPOSÉ et ne se refuse plus (windowOptOutEdgeToEdgeEnforcement
     * est désactivé sur Android 16). La WebView est donc dessinée SOUS les barres système : sans ce
     * gestionnaire, la barre d'outils du bas passerait derrière la barre de navigation (les 3 boutons)
     * et cesserait d'être cliquable — exactement ce que le test sur appareil doit vérifier.
     *
     * On rembourse par l'UNION barres système + encoche, puis on CONSOMME : le contenu reste dans la
     * zone sûre quel que soit le mode d'encoche retenu par la version d'Android, sans avoir à en
     * dépendre. C'est la réserve d'espace que le framework faisait lui-même avant targetSdk 36 :
     * l'apparence ne change donc pas.
     *
     * ⚠ CONSÉQUENCE À CONNAÎTRE : la WebView ne débordant plus sous les barres, `env(safe-area-inset-*)`
     * y vaut 0 et le rembourrage CSS du jeu devient inerte DANS L'APK. Il reste actif en web/PWA, où
     * c'est le navigateur qui gère la zone sûre. Les deux chemins ne se cumulent donc jamais.
     *
     * ⚠ Sur Android <= 15, le framework applique encore lui-même les insets au contenu : ce
     * gestionnaire reçoit alors des valeurs nulles et ne rembourre rien — pas de double marge.
     */
    private void applyInsetPadding() {
        web.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int l, t, r, b;
                if (Build.VERSION.SDK_INT >= 30) {
                    Insets safe = insets.getInsets(
                            WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                    l = safe.left; t = safe.top; r = safe.right; b = safe.bottom;
                } else {
                    l = insets.getSystemWindowInsetLeft();
                    t = insets.getSystemWindowInsetTop();
                    r = insets.getSystemWindowInsetRight();
                    b = insets.getSystemWindowInsetBottom();
                }
                v.setPadding(l, t, r, b);
                return Build.VERSION.SDK_INT >= 29
                        ? WindowInsets.CONSUMED
                        : insets.consumeSystemWindowInsets();
            }
        });
        web.requestApplyInsets();
    }

    /** Ouvre les URLs http/https dans le navigateur système ; garde le reste dans la WebView. */
    private boolean openExternally(Uri uri) {
        String scheme = uri != null ? uri.getScheme() : null;
        if ("http".equals(scheme) || "https".equals(scheme)) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    // ----------------------------------------------------------------------------------------
    // Mise à jour in-app (téléchargement + installation via PackageInstaller).
    // ----------------------------------------------------------------------------------------

    /** Pont exposé au JS sous le nom global window.ArchipelNative. */
    private final class WebBridge {
        @JavascriptInterface
        public boolean available() {
            return true;
        }

        /** Export de sauvegarde : écrit un fichier .txt dans le dossier Téléchargements. */
        @JavascriptInterface
        public void saveText(final String filename, final String content) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    writeDownload(filename, content);
                }
            });
        }

        @JavascriptInterface
        public void update(final String url) {
            // Variante MAGASIN : l'installation d'un binaire téléchargé est interdite par la
            // politique Google (et Apple 2.5.2). Le manifeste magasin ne déclare de toute façon
            // pas REQUEST_INSTALL_PACKAGES — c'est la seconde ligne de défense, côté code.
            // Le HTML magasin n'appelle jamais ce pont (`SELF_UPDATE = false` y masque déjà les
            // deux points d'entrée) : cette garde couvre le cas d'un asset mal apparié.
            if (!BuildConfig.SELF_UPDATE) return;
            if (url == null || url.isEmpty()) return;
            // Android 8+ : l'installation d'APK requiert l'autorisation « applis inconnues »
            // accordée à NOTRE app. Si absente, on ouvre l'écran de réglage et on réessaiera.
            if (!getPackageManager().canRequestPackageInstalls()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this,
                                "Autorisez l'installation depuis Archipel, puis retapez « Mettre à jour ».",
                                Toast.LENGTH_LONG).show();
                        try {
                            startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:" + getPackageName())));
                        } catch (Exception ignored) {
                        }
                    }
                });
                jsUpdate("error", 0);
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadAndInstall(url);
                }
            }).start();
        }
    }

    /** Écrit le contenu texte en .txt dans Téléchargements (MediaStore sur Android 10+,
     *  sinon dossier de l'app). Affiche un Toast indiquant l'emplacement. */
    private void writeDownload(String filename, String content) {
        String name = (filename == null || filename.isEmpty()) ? "archipel-sauvegarde.txt" : filename;
        if (content == null) content = "";
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.Downloads.DISPLAY_NAME, name);
                cv.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri == null) throw new Exception("insert null");
                OutputStream os = getContentResolver().openOutputStream(uri);
                os.write(content.getBytes("UTF-8"));
                os.flush();
                os.close();
                Toast.makeText(this, "Sauvegarde exportée dans Téléchargements : " + name,
                        Toast.LENGTH_LONG).show();
            } else {
                File dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                if (dir != null && !dir.exists()) dir.mkdirs();
                File f = new File(dir, name);
                FileOutputStream fo = new FileOutputStream(f);
                fo.write(content.getBytes("UTF-8"));
                fo.flush();
                fo.close();
                Toast.makeText(this, "Sauvegarde exportée : " + f.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Échec de l'export du fichier.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CHOOSER_REQUEST) {
            Uri[] result = null;
            if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
                result = new Uri[]{ data.getData() };
            }
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(result);
                filePathCallback = null;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void downloadAndInstall(String url) {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(20000);
            conn.connect();
            int code = conn.getResponseCode();
            // Redirection cross-host (GitHub release → CDN) : suivie manuellement au besoin.
            if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP
                    || code == HttpURLConnection.HTTP_SEE_OTHER || code == 307 || code == 308) {
                String loc = conn.getHeaderField("Location");
                conn.disconnect();
                conn = (HttpURLConnection) new URL(loc).openConnection();
                conn.setConnectTimeout(20000);
                conn.setReadTimeout(20000);
                conn.connect();
                code = conn.getResponseCode();
            }
            if (code != 200) {
                jsUpdate("error", 0);
                return;
            }
            int total = conn.getContentLength();
            File apk = new File(getCacheDir(), "update.apk");
            InputStream in = conn.getInputStream();
            FileOutputStream out = new FileOutputStream(apk);
            byte[] buf = new byte[16384];
            int read;
            long doneBytes = 0;
            int lastPct = -1;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                doneBytes += read;
                if (total > 0) {
                    int pct = (int) (doneBytes * 100 / total);
                    if (pct != lastPct) {
                        lastPct = pct;
                        jsUpdate("download", pct);
                    }
                }
            }
            out.flush();
            out.close();
            in.close();
            installApk(apk);
        } catch (Exception e) {
            jsUpdate("error", 0);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private void installApk(File apk) {
        try {
            jsUpdate("install", 100);
            PackageInstaller pi = getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params =
                    new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = pi.createSession(params);
            PackageInstaller.Session session = pi.openSession(sessionId);
            OutputStream sout = session.openWrite("archipel", 0, apk.length());
            FileInputStream fin = new FileInputStream(apk);
            byte[] buf = new byte[16384];
            int r;
            while ((r = fin.read(buf)) != -1) sout.write(buf, 0, r);
            session.fsync(sout);
            fin.close();
            sout.close();
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) flags |= PendingIntent.FLAG_MUTABLE;
            Intent intent = new Intent(INSTALL_ACTION).setPackage(getPackageName());
            PendingIntent pending = PendingIntent.getBroadcast(this, sessionId, intent, flags);
            session.commit(pending.getIntentSender());
            session.close();
        } catch (Exception e) {
            jsUpdate("error", 0);
        }
    }

    private void registerInstallReceiver() {
        installReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                        PackageInstaller.STATUS_FAILURE);
                if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    // L'OS demande la confirmation utilisateur : on lance son écran d'installation.
                    Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                    if (confirm != null) {
                        confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        try {
                            startActivity(confirm);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (status != PackageInstaller.STATUS_SUCCESS) {
                    jsUpdate("error", 0);
                    String msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
                    Toast.makeText(MainActivity.this,
                            "Installation interrompue" + (msg != null ? " : " + msg : ""),
                            Toast.LENGTH_LONG).show();
                }
            }
        };
        IntentFilter filter = new IntentFilter(INSTALL_ACTION);
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(installReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(installReceiver, filter);
        }
    }

    /** Notifie le JS de l'avancement (window.__archipelUpdate(state, pct)), sur le thread UI. */
    private void jsUpdate(final String state, final int pct) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (web != null) {
                    web.evaluateJavascript(
                            "window.__archipelUpdate&&window.__archipelUpdate('" + state + "'," + pct + ")",
                            null);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (installReceiver != null) {
            try {
                unregisterReceiver(installReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemBars();
    }

    private void hideSystemBars() {
        // On masque seulement la barre de statut (en haut). La barre de navigation
        // (les 3 boutons Android, en bas) reste visible et son espace est réservé par
        // `applyInsetPadding()` : le jeu se place AU-DESSUS, donc les boutons ne
        // recouvrent plus l'UI du bas.
        // ⚠ `setSystemUiVisibility` est déprécié depuis l'API 30 et n'est plus fiable une fois
        // l'edge-to-edge imposé : au-delà, on passe par WindowInsetsController. Masquer la barre
        // de statut met son inset à 0, donc le rembourrage du haut retombe à l'encoche seule.
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            View d = getWindow().getDecorView();
            d.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

    @Override
    public void onBackPressed() {
        if (web != null && web.canGoBack()) {
            web.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
