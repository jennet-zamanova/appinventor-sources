package com.google.appinventor.shared.rpc.project.youngandroid;

import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.BLOCKLY_SOURCE_EXTENSION;
import static com.google.appinventor.common.constants.YoungAndroidStructureConstants.CODEBLOCKS_SOURCE_EXTENSION;

import com.google.appinventor.shared.rpc.project.ProjectRootNode;
import com.google.appinventor.shared.storage.StorageUtil;


/**
 * Young Android blocks source file node in the project tree.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public final class YoungAndroidBlocksEmptyNode extends YoungAndroidBlocksNode {
  /**
   * Creates a new Young Android blocks source file project node.
   *
   * @param fileId  file id
   */
  private long projectID;

  public YoungAndroidBlocksEmptyNode() {
    super("$diff$empty$");
    projectID = -1;
  }

  public YoungAndroidBlocksEmptyNode(long projectId) {
    super("$diff$empty$");
    projectID = projectId;
  }

  public long getProjectId() {
    return projectID;
  }

  public ProjectRootNode getProjectRoot() {
    return new YoungAndroidProjectEmptyNode();
  }

  public static String getCodeblocksFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + CODEBLOCKS_SOURCE_EXTENSION;
  }

  public static String getBlocklyFileId(String qualifiedName) {
    return SRC_PREFIX + qualifiedName.replace('.', '/')
        + BLOCKLY_SOURCE_EXTENSION;
  }
}
