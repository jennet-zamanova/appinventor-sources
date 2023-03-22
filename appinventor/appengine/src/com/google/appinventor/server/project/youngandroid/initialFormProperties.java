package com.google.appinventor.server.project.youngandroid;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
// import com.google.appinventor.client.Ode;
// import com.google.appinventor.client.OdeMessages;
// import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.common.utils.StringUtils;
import com.google.appinventor.common.version.GitBuildId;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.server.CrashReport;
import com.google.appinventor.server.FileExporter;
import com.google.appinventor.server.FileExporterImpl;
import com.google.appinventor.server.FileImporter;
import com.google.appinventor.server.FileImporterException;
import com.google.appinventor.server.FileImporterImpl;
import com.google.appinventor.server.Server;
import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.project.CommonProjectService;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.project.youngandroid.*;
import com.google.appinventor.server.properties.json.ServerJsonParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.util.UriBuilder;
import com.google.appinventor.shared.properties.json.JSONParser;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.NewProjectParameters;
import com.google.appinventor.shared.rpc.project.Project;
import com.google.appinventor.shared.rpc.project.ProjectNode;
import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.rpc.project.ProjectSourceZip;
import com.google.appinventor.shared.rpc.project.RawFile;
import com.google.appinventor.shared.rpc.project.TextFile;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidAssetsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidBlocksNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidComponentsFolder;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidFormNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidPackageNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceFolderNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidSourceNode;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidYailNode;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.settings.Settings;
import com.google.appinventor.shared.settings.SettingsConstants;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.appinventor.shared.youngandroid.YoungAndroidSourceAnalyzer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.Logger;

public class initialFormProperties {

    public String initialProperties (String qualifiedName, NewYoungAndroidProjectParameters youngAndroidParams){
        Logger LOG = Logger.getLogger(initialFormProperties.class.getName());
        final int lastDotPos = qualifiedName.lastIndexOf('.');
        String packageName = qualifiedName.split("\\.")[2];
        String formName = qualifiedName.substring(lastDotPos + 1);
        String themeName = youngAndroidParams.getThemeName();
        String blocksToolkit = youngAndroidParams.getBlocksToolkit();
        LOG.info("PROBLEM IS HERE!!!!! =" + blocksToolkit);
        if (blocksToolkit.length() > 3){
            blocksToolkit = "{" + blocksToolkit.substring(2, blocksToolkit.length()-2);
        }
        LOG.info("blocksToolkit here !!!!!!!!=" + blocksToolkit);
        String newString = "#|\n$JSON\n" +
            "{\"authURL\":[]," +
            "\"YaVersion\":\"" + YaVersion.YOUNG_ANDROID_VERSION + "\",\"Source\":\"Form\"," +
            "\"Properties\":{\"$Name\":\"" + formName + "\",\"$Type\":\"Form\"," +
            "\"$Version\":\"" + YaVersion.FORM_COMPONENT_VERSION + "\",\"Uuid\":\"" + 0 + "\"," +
            "\"Title\":\"" + formName + "\",\"AppName\":\"" + packageName +"\",\"Theme\":\"" + 
            themeName + "\",\"BlocksSubset\":\"" + blocksToolkit + "\"}}\n|#";
        LOG.info("Form file contents =" + newString);
        return newString;
    }
    
}
