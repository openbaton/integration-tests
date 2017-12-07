package org.openbaton.integration.test;

import org.ini4j.Profile;
import org.openbaton.catalogue.nfvo.Action;
import org.openbaton.integration.test.exceptions.IntegrationTestException;
import org.openbaton.integration.test.testers.*;
import org.openbaton.integration.test.utils.SubTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.criteria.CriteriaBuilder;

public class TaskConfigurator {

  private static Logger log = LoggerFactory.getLogger(TaskConfigurator.class);

  static void configureNetworkServiceDescriptorWait(SubTask subTask, Profile.Section currentSection)
      throws IntegrationTestException {
    NetworkServiceDescriptorWait w = (NetworkServiceDescriptorWait) subTask;
    w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    if (action == null || action.isEmpty()) {
      throw new IntegrationTestException("action for VirtualNetworkFunctionRecordWait not set");
    }
    w.setAction(Action.valueOf(action));
  }

  static void configureNetworkServiceDescriptorDelete(
      SubTask instance, Profile.Section currentSection) {
    //cast and get specific properties
  }

  static void configureVirtualNetworkFunctionDescriptorDelete(
      SubTask subtask, Profile.Section currentSection) {
    VirtualNetworkFunctionDescriptorDelete w = (VirtualNetworkFunctionDescriptorDelete) subtask;
    String vnfdType = currentSection.get("vnf-type");
    String vnfdName = currentSection.get("vnf-name");
    if (vnfdType != null) {
      w.setVnfdType(vnfdType);
    }
    if (vnfdName != null) {
      w.setVnfdName(vnfdName);
    }
  }

  static void configureVirtualNetworkFunctionRecordWait(
      SubTask subTask, Profile.Section currentSection) throws IntegrationTestException {
    VirtualNetworkFunctionRecordWait w = (VirtualNetworkFunctionRecordWait) subTask;
    w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    String vnfType = currentSection.get("vnf-type");
    if (action == null || action.isEmpty()) {
      throw new IntegrationTestException("action for VirtualNetworkFunctionRecordWait not set");
    }
    if (vnfType == null || vnfType.isEmpty()) {
      throw new IntegrationTestException("vnf-type property not set");
    }
    w.setAction(Action.valueOf(action));
    w.setVnfrType(vnfType);
  }

  static void configureNetworkServiceRecordWait(SubTask instance, Profile.Section currentSection)
      throws IntegrationTestException {
    NetworkServiceRecordWait w = (NetworkServiceRecordWait) instance;
    w.setTimeout(Integer.parseInt(currentSection.get("timeout", "5")));

    String action = currentSection.get("action");
    if (action == null) {
      throw new IntegrationTestException("action for NetworkServiceRecordWait not set");
    }
    w.setAction(Action.valueOf(action));
  }

  static void configureNetworkServiceRecordCreate(
      SubTask instance, Profile.Section currentSection) {
    //cast and get specific properties
  }

  static void configureNetworkServiceRecordDelete(
      SubTask instance, Profile.Section currentSection) {
    //cast and get specific properties
  }

  static void configureNetworkServiceDescriptorCreate(
      SubTask instance, Profile.Section currentSection) {
    NetworkServiceDescriptorCreate w = (NetworkServiceDescriptorCreate) instance;
    w.setFileName(currentSection.get("name-file"));
    w.setExpectedToFail(currentSection.get("expected-to-fail"));
  }

  static void configureVimInstanceCreate(SubTask instance, Profile.Section currentSection) {
    VimInstanceCreate w = (VimInstanceCreate) instance;
    w.setFileName(currentSection.get("name-file"));
    w.setAsUser(currentSection.get("as-user-name"));
    w.setAsUserPassword(currentSection.get("as-user-password"));
    w.setExpectedToFail(currentSection.get("expected-to-fail"));
    w.setInProject(currentSection.get("in-project"));
  }

  static void configureVimInstanceDelete(SubTask instance, Profile.Section currentSection) {
    VimInstanceDelete w = (VimInstanceDelete) instance;
    w.setAsUser(currentSection.get("as-user-name"));
    w.setAsUserPassword(currentSection.get("as-user-password"));
    w.setExpectedToFail(currentSection.get("expected-to-fail"));
    w.setInProject(currentSection.get("in-project"));
  }

  static void configureGenericServiceTester(SubTask subTask, Profile.Section currentSection) {
    GenericServiceTester t = (GenericServiceTester) subTask;
    Boolean stop = false;
    String vnfrType = currentSection.get("vnf-type");
    String vmScriptsPath = currentSection.get("vm-scripts-path");
    String user = currentSection.get("user-name");
    if (vnfrType != null) {
      t.setVnfrType(vnfrType);
    }

    if (vmScriptsPath != null) {
      t.setVmScriptsPath(vmScriptsPath);
    }

    String netName = currentSection.get("net-name");
    if (netName != null) {
      t.setVirtualLink(netName);
    }

    if (user != null) {
      t.setUserName(user);
    }

    for (int i = 1; !stop; i++) {
      String scriptName = currentSection.get("script-" + i);
      if (scriptName == null || scriptName.isEmpty()) {
        stop = true;
        continue;
      }
      t.addScript(scriptName);
    }
  }

  static void configureScaleOut(SubTask subTask, Profile.Section currentSection) {
    ScaleOut t = (ScaleOut) subTask;
    String vnfrType = currentSection.get("vnf-type");
    String virtualLink = currentSection.get("virtual-link");
    String floatingIp = currentSection.get("floating-ip");
    if (vnfrType != null) {
      t.setVnfrType(vnfrType);
    }

    if (virtualLink != null) {
      t.setVirtualLink(virtualLink);
    }

    if (floatingIp != null) {
      t.setFloatingIp(floatingIp);
    }
  }

  static void configureScaleIn(SubTask subTask, Profile.Section currentSection) {
    ScaleIn t = (ScaleIn) subTask;
    String vnfrType = currentSection.get("vnf-type");
    if (vnfrType != null) {
      t.setVnfrType(vnfrType);
    }
  }

  static void configureScalingTester(SubTask subTask, Profile.Section currentSection) {
    ScalingTester t = (ScalingTester) subTask;
    String vnfrType = currentSection.get("vnf-type");
    String vnfcCount = currentSection.get("vnfc-count");
    if (vnfrType != null) {
      t.setVnfrType(vnfrType);
    }

    if (vnfcCount != null) {
      t.setVnfcCount(vnfcCount);
    }
  }

  static void configurePackageUpload(SubTask instance, Profile.Section currentSection) {
    PackageUpload p = (PackageUpload) instance;
    p.setPackageName(currentSection.get("package-name"));
  }

  static void configurePackageDelete(SubTask instance, Profile.Section currentSection) {
    PackageDelete p = (PackageDelete) instance;
    p.setPackageName(currentSection.get("package-name"));
  }

  static void configureVnfrStatusTester(SubTask instance, Profile.Section currentSection) {
    VNFRStatusTester t = (VNFRStatusTester) instance;
    String status = currentSection.get("status");
    if (status != null) {
      t.setStatus(status);
    }

    String vnfrType = currentSection.get("vnf-type");
    if (vnfrType != null) {
      t.setVnfrType(vnfrType);
    }
  }

  static void configurePause(SubTask instance, Profile.Section currentSection)
      throws IntegrationTestException {
    Pause p = (Pause) instance;
    String d = currentSection.get("duration");
    try {
      int duration = Integer.parseInt(d);
      p.setDuration(duration);
    } catch (NumberFormatException e) {
      throw new IntegrationTestException(
          "The duration field of Pause is not an integer so we cannot use it");
    }
  }

  static void configureUserCreate(SubTask instance, Profile.Section currentSection) {
    UserCreate userCreate = (UserCreate) instance;
    userCreate.setExpectedToFail(currentSection.get("expected-to-fail"));
    userCreate.setNewUserName(currentSection.get("new-user-name"));
    userCreate.setNewUserPwd(currentSection.get("new-user-password"));
    userCreate.setAsUser(currentSection.get("as-user-name"));
    userCreate.setAsUserPassword(currentSection.get("as-user-password"));
    userCreate.setUserIsAdmin(currentSection.get("new-user-is-admin"));
    userCreate.setUserProjects(currentSection.get("user-projects"));
    userCreate.setGuestProjects(currentSection.get("guest-projects"));
    userCreate.setEnabled(currentSection.get("enabled"));
  }

  static void configureUserDelete(SubTask instance, Profile.Section currentSection) {
    UserDelete userDelete = (UserDelete) instance;
    userDelete.setExpectedToFail(currentSection.get("expected-to-fail"));
    userDelete.setAsUser(currentSection.get("as-user-name"));
    userDelete.setUserPassword(currentSection.get("as-user-password"));
    userDelete.setUserToDelete(currentSection.get("user-to-delete"));
  }

  static void configureUserUpdate(SubTask instance, Profile.Section currentSection) {
    UserUpdate userUpdate = (UserUpdate) instance;
    userUpdate.setExpectedToFail(currentSection.get("expected-to-fail"));
    userUpdate.setNewUserName(currentSection.get("user-name-new"));
    userUpdate.setNewUserPwd(currentSection.get("user-password-new"));
    userUpdate.setAsUser(currentSection.get("as-user-name"));
    userUpdate.setUserPassword(currentSection.get("as-user-password"));
    userUpdate.setUserIsAdmin(currentSection.get("user-is-admin"));
    userUpdate.setUserProjects(currentSection.get("user-projects"));
    userUpdate.setGuestProjects(currentSection.get("guest-projects"));
    userUpdate.setEnabled(currentSection.get("enabled"));
    userUpdate.setOldUserName(currentSection.get("user-name-old"));
  }

  static void configureProjectCreate(SubTask instance, Profile.Section currentSection) {
    ProjectCreate projectCreate = (ProjectCreate) instance;
    projectCreate.setExpectedToFail(currentSection.get("expected-to-fail"));
    projectCreate.setAsUser(currentSection.get("as-user-name"));
    projectCreate.setUserPassword(currentSection.get("as-user-password"));
    projectCreate.setProjectName(currentSection.get("project-name"));
  }

  static void configureProjectDelete(SubTask instance, Profile.Section currentSection) {
    ProjectDelete projectDelete = (ProjectDelete) instance;
    projectDelete.setExpectedToFail(currentSection.get("expected-to-fail"));
    projectDelete.setAsUser(currentSection.get("as-user-name"));
    projectDelete.setUserPassword(currentSection.get("as-user-password"));
    projectDelete.setProjectToDelete(currentSection.get("project-name"));
  }
}
