package fr.archipel.industry;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.FrameLayout;
import android.widget.TextView;
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
    /** Étiquette de journal du diagnostic d'insets (`adb logcat -s ArchipelInsets`). */
    private static final String TAG = "ArchipelInsets";

    private WebView web;
    /**
     * Vue racine que NOUS contrôlons, insérée entre `android.R.id.content` et la WebView.
     * C'est le point le plus haut de la hiérarchie qu'une application peut se donner sans
     * détourner le DecorView : le gestionnaire d'insets y est posé, de sorte qu'aucune vue
     * intermédiaire ne puisse les avoir consommés avant lui.
     */
    private FrameLayout root;
    /** Afficheur de diagnostic P4, présent UNIQUEMENT dans les builds `-PinsetDiag=true`. */
    private TextView diag;
    private final StringBuilder diagInsets = new StringBuilder();
    private final StringBuilder diagPadding = new StringBuilder();
    private final StringBuilder diagLate = new StringBuilder();
    private final StringBuilder diagCss = new StringBuilder();
    private int insetPass = 0;
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

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Le relevé CSS ne peut être pris qu'une fois la page chargée ET mise en page.
                // 1,2 s de marge : l'asset fait ~3,7 Mo et le premier layout n'est pas immédiat.
                if (BuildConfig.INSET_DIAG) view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        probeCssInsets();
                    }
                }, 1200);
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

        // ⚠ P3 — la WebView n'est PLUS la vue de contenu directe : elle est enveloppée dans une
        // racine que nous contrôlons. Voir setUpInsets() : le gestionnaire d'insets doit être posé
        // le plus haut possible, sinon une vue intermédiaire peut les avoir déjà consommés.
        root = new FrameLayout(this);
        root.addView(web, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (BuildConfig.INSET_DIAG) addDiagOverlay();

        setContentView(root);
        setUpInsets();
        web.loadUrl("file:///android_asset/index.html");
    }

    /**
     * P4 — LE REMBOURRAGE VIENT DU CSS, ET DE LUI SEUL. Il n'y a plus de rembourrage natif.
     *
     * Contexte : targetSdk 36 impose l'edge-to-edge (`windowOptOutEdgeToEdgeEnforcement` est
     * déprécié ET désactivé sur Android 16), la fenêtre est donc dessinée SOUS les barres système.
     * Le lot P2 avait répondu par un rembourrage natif ; il ne s'appliquait pas (les insets étaient
     * consommés avant lui) et la barre d'outils du bas passait sous les 3 boutons de navigation.
     *
     * Le lot P3 a mis DEUX corrections en concurrence sur appareil. **Mesuré par Ethan sur Galaxy
     * S25 FE, navigation à 3 boutons**, avec `sys t0 b135` et `cut t82` des deux côtés :
     *
     *   A — rembourrage natif : `pad t0 b135`, racine 2340, WebView 2205 → la WebView est rétrécie.
     *   B — aucun natif       : `pad t0 b0`,   racine 2340, WebView 2340 → la WebView occupe tout
     *                           l'écran, ET la barre ACTIONS est dégagée.
     *
     * B dégage la barre sans qu'aucun pixel natif ne soit rembourré : **une WebView Android
     * renseigne donc bien `safe-area-inset-bottom` pour la barre de NAVIGATION**, et pas seulement
     * pour l'encoche comme on le craignait. C'est le CSS `env(safe-area-inset-*)` du lot A qui
     * opère, seul.
     *
     * **B est retenue (décision d'Ethan)** : un seul mécanisme de rembourrage pour l'APK, le web et
     * la PWA. Avec A, deux chemins produisaient le même résultat sur deux canaux, et toute retouche
     * du HUD aurait dû être vérifiée sur les deux. Cette dette est annulée.
     *
     * ⚠ IL N'Y A DONC PLUS RIEN À FAIRE ICI QUE DEUX CHOSES, et surtout PAS de `setPadding` :
     *  1. `setDecorFitsSystemWindows(false)` — PRÉREQUIS INDISPENSABLE de B : sans lui la WebView
     *     n'est pas disposée sous les barres, elle reçoit des insets nuls et le CSS ne réserve
     *     rien. Le retirer casserait le rembourrage sur les trois paquets.
     *  2. `requestApplyInsets()` à chaque changement de configuration (onConfigurationChanged),
     *     l'activité déclarant `configChanges` donc n'étant pas recréée.
     *
     * Le gestionnaire ci-dessous n'applique RIEN et ne consomme RIEN : il ne sert qu'au relevé de
     * diagnostic, et il rend les insets intacts pour que
     * `ViewGroup.dispatchApplyWindowInsets` les transmette à la WebView. Il disparaît avec le
     * relevé.
     */
    private void setUpInsets() {
        // PRÉREQUIS DE B — ne pas retirer : sans lui la WebView n'est pas sous les barres et
        // `env(safe-area-inset-*)` y vaut 0, donc plus aucun rembourrage nulle part.
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        root.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                // OBSERVATION SEULE. Aucun padding, aucune consommation : les insets descendent
                // intacts jusqu'à la WebView, qui renseigne env(safe-area-inset-*).
                insetPass++;
                recordInsets(insets);
                recordPadding(v);
                return insets;
            }
        });
        root.requestApplyInsets();

        // Relevé TARDIF : si le rembourrage a été écrasé par une passe de layout ultérieure
        // (4e cause envisagée), c'est ici que ça se voit — le padding relu 2,5 s après le
        // chargement ne correspondra plus à celui qu'on vient d'appliquer.
        if (BuildConfig.INSET_DIAG) {
            root.postDelayed(new Runnable() {
                @Override
                public void run() {
                    recordLate();
                }
            }, 2500);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // L'activité déclare configChanges (orientation|screenSize|…) : elle n'est PAS recréée,
        // donc rien ne redemanderait les insets sans cet appel.
        if (root != null) root.requestApplyInsets();
    }

    // ------------------------------------------------------------------------------------------
    // Diagnostic P3 — présent uniquement dans les builds de test (-PinsetDiag=true).
    // Il n'y a pas d'appareil côté développement : c'est le seul moyen de relever CE QUE LE
    // GESTIONNAIRE REÇOIT RÉELLEMENT sans imposer un aller-retour supplémentaire au testeur.
    // Les mêmes valeurs partent aussi dans le journal (adb logcat -s ArchipelInsets).
    // ------------------------------------------------------------------------------------------

    private void addDiagOverlay() {
        diag = new TextView(this);
        diag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        diag.setTextColor(0xFFFFF176);
        diag.setBackgroundColor(0xCC000000);
        diag.setPadding(6, 4, 6, 4);
        diag.setTypeface(android.graphics.Typeface.MONOSPACE);
        diag.setText("P4 — en attente d'insets…");
        // Le relevé recouvre le haut du HUD : un appui le fait disparaître pour laisser
        // inspecter la barre ACTIONS du bas sans gêne.
        diag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.GONE);
            }
        });
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        root.addView(diag, lp);
    }

    private static String insetLine(String name, Insets i) {
        return name + " t" + i.top + " b" + i.bottom + " l" + i.left + " r" + i.right + "\n";
    }

    private void recordInsets(WindowInsets insets) {
        diagInsets.setLength(0);
        diagInsets.append("P4 pass=").append(insetPass)
                .append(" api=").append(Build.VERSION.SDK_INT)
                // isConsumed() vrai ici = un parent a consommé avant nous (2e/3e cause).
                .append(" consumed=").append(insets.isConsumed()).append('\n');
        if (Build.VERSION.SDK_INT >= 30) {
            diagInsets.append(insetLine("sys", insets.getInsets(WindowInsets.Type.systemBars())));
            diagInsets.append(insetLine("nav",
                    insets.getInsets(WindowInsets.Type.navigationBars())));
            diagInsets.append(insetLine("cut",
                    insets.getInsets(WindowInsets.Type.displayCutout())));
        }
        diagInsets.append("old t").append(insets.getSystemWindowInsetTop())
                .append(" b").append(insets.getSystemWindowInsetBottom())
                .append(" l").append(insets.getSystemWindowInsetLeft())
                .append(" r").append(insets.getSystemWindowInsetRight()).append('\n');
        pushDiag();
    }

    private void recordPadding(View v) {
        diagPadding.setLength(0);
        diagPadding.append("pad t").append(v.getPaddingTop())
                .append(" b").append(v.getPaddingBottom())
                .append(" l").append(v.getPaddingLeft())
                .append(" r").append(v.getPaddingRight()).append('\n');
        pushDiag();
    }

    private void recordLate() {
        if (root == null) return;
        diagLate.setLength(0);
        diagLate.append("+2.5s pad b").append(root.getPaddingBottom())
                .append(" root ").append(root.getHeight())
                .append(" web ").append(web != null ? web.getHeight() : -1)
                .append(" dpi ").append(getResources().getDisplayMetrics().densityDpi).append('\n');
        pushDiag();
    }

    /**
     * Relève `env(safe-area-inset-*)` TEL QUE LA WEBVIEW LE CALCULE — c'est la seule mesure qui
     * dise vraiment ce que le CSS du jeu réserve. Le relevé natif (`sys`/`nav`/`cut`) donne ce que
     * la fenêtre reçoit ; il ne dit pas ce que la page en fait.
     *
     * ⚠ DEUX UNITÉS, NE PAS LES CONFONDRE : `env()` rend des **px CSS**, les insets natifs des
     * **px physiques**. Sur un écran à `devicePixelRatio = 3`, un inset natif de 82 px se lit
     * ~27,3 px CSS. Le relevé publie donc les deux (`css` puis `phy`, = css × dpr) pour que la
     * comparaison avec `sys`/`cut` ait un sens.
     */
    private void probeCssInsets() {
        if (web == null) return;
        final String js =
                "(function(){try{var d=document.createElement('div');"
                + "d.style.cssText='position:fixed;top:0;left:0;width:0;height:0;visibility:hidden;"
                + "padding-top:env(safe-area-inset-top);padding-bottom:env(safe-area-inset-bottom);"
                + "padding-left:env(safe-area-inset-left);padding-right:env(safe-area-inset-right);';"
                + "document.documentElement.appendChild(d);var s=getComputedStyle(d);"
                + "var r=[s.paddingTop,s.paddingBottom,s.paddingLeft,s.paddingRight]"
                + ".map(function(v){return parseFloat(v)||0;});"
                + "d.parentNode.removeChild(d);"
                + "return r.join(',')+','+(window.devicePixelRatio||1);}catch(e){return 'err';}})()";
        web.evaluateJavascript(js, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                diagCss.setLength(0);
                String v = value == null ? "" : value.replace("\"", "");
                String[] p = v.split(",");
                if (p.length >= 5) {
                    float dpr = parseF(p[4], 1f);
                    diagCss.append("sa css t").append(p[0]).append(" b").append(p[1])
                            .append(" l").append(p[2]).append(" r").append(p[3])
                            .append(" dpr").append(p[4]).append('\n')
                            .append("sa phy t").append(Math.round(parseF(p[0], 0f) * dpr))
                            .append(" b").append(Math.round(parseF(p[1], 0f) * dpr))
                            .append(" l").append(Math.round(parseF(p[2], 0f) * dpr))
                            .append(" r").append(Math.round(parseF(p[3], 0f) * dpr)).append('\n');
                } else {
                    diagCss.append("sa ?").append(v).append('\n');
                }
                pushDiag();
            }
        });
    }

    private static float parseF(String s, float def) {
        try {
            return Float.parseFloat(s.trim());
        } catch (Exception e) {
            return def;
        }
    }

    private void pushDiag() {
        String txt = diagInsets.toString() + diagPadding + diagLate + diagCss;
        Log.i(TAG, txt.replace('\n', '|'));
        if (diag != null) diag.setText(txt.trim());
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
        // On masque seulement la barre de statut (en haut) — c'est le plein écran voulu, et
        // l'absence de barre d'état en jeu est un comportement confirmé, pas un défaut. La barre
        // de navigation (les 3 boutons Android, en bas) reste visible ; réserver son espace est
        // le travail de `setUpInsets()` (variante A) ou du CSS de la page (variante B).
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
