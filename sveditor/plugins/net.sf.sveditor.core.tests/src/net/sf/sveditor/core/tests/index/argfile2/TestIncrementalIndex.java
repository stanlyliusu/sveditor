package net.sf.sveditor.core.tests.index.argfile2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.sf.sveditor.core.BundleUtils;
import net.sf.sveditor.core.SVCorePlugin;
import net.sf.sveditor.core.Tuple;
import net.sf.sveditor.core.db.ISVDBItemBase;
import net.sf.sveditor.core.db.SVDBFile;
import net.sf.sveditor.core.db.index.ISVDBIndex;
import net.sf.sveditor.core.db.index.ISVDBIndexChangeListener;
import net.sf.sveditor.core.db.index.SVDBDeclCacheItem;
import net.sf.sveditor.core.db.index.SVDBIndexResourceChangeEvent;
import net.sf.sveditor.core.db.index.SVDBIndexResourceChangeEvent.Type;
import net.sf.sveditor.core.db.index.SVDBWSFileSystemProvider;
import net.sf.sveditor.core.db.index.argfile.SVDBArgFileIndex;
import net.sf.sveditor.core.db.index.builder.ISVDBIndexChangePlan;
import net.sf.sveditor.core.db.index.cache.ISVDBIndexCache;
import net.sf.sveditor.core.db.project.SVDBProjectData;
import net.sf.sveditor.core.db.project.SVDBProjectManager;
import net.sf.sveditor.core.db.project.SVProjectFileWrapper;
import net.sf.sveditor.core.db.search.ISVDBFindNameMatcher;
import net.sf.sveditor.core.db.search.SVDBFindByNameMatcher;
import net.sf.sveditor.core.tests.IndexTestUtils;
import net.sf.sveditor.core.tests.SVCoreTestCaseBase;
import net.sf.sveditor.core.tests.SVCoreTestsPlugin;
import net.sf.sveditor.core.tests.utils.TestUtils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;

public class TestIncrementalIndex extends SVCoreTestCaseBase implements ISVDBIndexChangeListener {
	private int						m_index_update;
	
	@Override
	public void index_changed(int reason, SVDBFile file) {
		// TODO Auto-generated method stub
		System.out.println("index_changed");
	}

	@Override
	public void index_rebuilt() {
		System.out.println("index_rebuilt");
		m_index_update++;
	}

	public void testRootFileChange_1() {
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		ISVDBIndex index = setupProject(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		// Now, tell the index that pkg1.sv changed, and check the plan
		List<SVDBIndexResourceChangeEvent> changes = new ArrayList<SVDBIndexResourceChangeEvent>();
		changes.add(new SVDBIndexResourceChangeEvent(Type.CHANGE, project_path + "/pkg1.sv"));
		
		ISVDBIndexChangePlan plan;
		
		plan = index.createIndexChangePlan(null);
		System.out.println("plan=" + plan);
		
		plan = index.createIndexChangePlan(changes);
		System.out.println("plan=" + plan);
		
		index.execIndexChangePlan(new NullProgressMonitor(), plan);
	}

	public void testRootFileChange_2() {
		SVCorePlugin.getDefault().setDebugLevel(LEVEL_MAX);
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		Tuple<ISVDBIndex, IProject> setup_data = setupProject_2(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		ISVDBIndex index = setup_data.first();
		IProject p = setup_data.second();
		index.addChangeListener(this);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {}
		
		List<SVDBDeclCacheItem> result;
		
		result = index.findGlobalScopeDecl(
				new NullProgressMonitor(), "pkg1_cls3", 
				new SVDBFindByNameMatcher());
		assertEquals(0, result.size());
	
		// Create a new class
		TestUtils.copy(
				"class pkg1_cls3;\n" +
				"	int		cls3_field;\n" +
				"endclass\n",
				p.getFile("simple_project1/pkg1_cls3.svh"));
		
		// Now, change pkg1.sv 
		String pkg1_sv = TestUtils.read(p.getFile("simple_project1/pkg1.sv"));
		TestUtils.copy(
				"package pkg1;\n" +
				"	`include \"pkg1_cls1.svh\"\n" +
				"	`include \"pkg1_cls2.svh\"\n" +
				"	`include \"pkg1_cls3.svh\"\n" +
				"endpackage\n",
				p.getFile("simple_project1/pkg1.sv"));
		
		System.out.println("write updated file");
		
		for (int i=0; i<100; i++) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			if (m_index_update > 0) {
				break;
			}
		}
		
		result = index.findGlobalScopeDecl(
				new NullProgressMonitor(), "pkg1_cls3", 
				new SVDBFindByNameMatcher());
		assertEquals(1, result.size());
	
		ISVDBItemBase it = result.get(0).getSVDBItem();
		assertNotNull(it);
	}
	
	public void testIncFileChange_1() {
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		ISVDBIndex index = setupProject(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		IndexTestUtils.assertFileHasElements(index, "pkg1_cls1", "pkg1_cls2");
		
		// Now, change pkg1_cls1
		TestUtils.copy_ws(
				"class pkg1_cls11;\n" +
				"endclass\n",
				project_path + "/pkg1_cls1.svh");
		
		// Now, tell the index that pkg1.sv changed, and check the plan
		List<SVDBIndexResourceChangeEvent> changes = new ArrayList<SVDBIndexResourceChangeEvent>();
		changes.add(new SVDBIndexResourceChangeEvent(Type.CHANGE, project_path + "/pkg1_cls1.svh"));
		
		ISVDBIndexChangePlan plan;
		
		plan = index.createIndexChangePlan(null);
		System.out.println("plan=" + plan);
		
		plan = index.createIndexChangePlan(changes);
		System.out.println("plan=" + plan);
		
		index.execIndexChangePlan(new NullProgressMonitor(), plan);
		
		IndexTestUtils.assertFileHasElements(index, "pkg1_cls11", "pkg1_cls2");
		IndexTestUtils.assertDoesNotContain(index, "pkg1_cls1");
	}

	public void testArgFileChange_1() {
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		ISVDBIndex index = setupProject(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		// Now, tell the index that pkg1.sv changed, and check the plan
		List<SVDBIndexResourceChangeEvent> changes = new ArrayList<SVDBIndexResourceChangeEvent>();
		changes.add(new SVDBIndexResourceChangeEvent(Type.CHANGE, project_path + "/simple_project1.f"));
		
		ISVDBIndexChangePlan plan;
		
		plan = index.createIndexChangePlan(null);
		System.out.println("plan=" + plan);
		
		plan = index.createIndexChangePlan(changes);
		System.out.println("plan=" + plan);
		
		index.execIndexChangePlan(new NullProgressMonitor(), plan);
	}

	public void testIrrelevantChange_1() {
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		ISVDBIndex index = setupProject(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		// Now, tell the index that pkg1.sv changed, and check the plan
		List<SVDBIndexResourceChangeEvent> changes = new ArrayList<SVDBIndexResourceChangeEvent>();
		changes.add(new SVDBIndexResourceChangeEvent(Type.CHANGE, project_path + "/missing_file.sv"));
		
		ISVDBIndexChangePlan plan;
		
		plan = index.createIndexChangePlan(null);
		System.out.println("plan=" + plan);
		
		plan = index.createIndexChangePlan(changes);
		System.out.println("plan=" + plan);
		
		index.execIndexChangePlan(new NullProgressMonitor(), plan);
	}
	
	public void testTearDownRebuild() {
		String project_path = "${workspace_loc}/" + getName() + "/simple_project1";
		
		ISVDBIndex index = setupProject(
				"/data/index_file_change/simple_project1",
				project_path + "/simple_project1.f");
		
		// Now, tell the index that pkg1.sv changed, and check the plan
		List<SVDBIndexResourceChangeEvent> changes = new ArrayList<SVDBIndexResourceChangeEvent>();
		changes.add(new SVDBIndexResourceChangeEvent(Type.CHANGE, project_path + "/pkg1.sv"));
		
		ISVDBIndexChangePlan plan;
		
		plan = index.createIndexChangePlan(null);
		System.out.println("plan=" + plan);
		
		plan = index.createIndexChangePlan(changes);
		System.out.println("plan=" + plan);
		
		index.execIndexChangePlan(new NullProgressMonitor(), plan);
	}	
	
	private ISVDBIndex setupProject(
			String			data_dir,
			String			argfile) {
		Tuple<ISVDBIndex, IProject> setup_data = setupProject_2(data_dir, argfile);
		return setup_data.first();
	}
	
	private Tuple<ISVDBIndex, IProject> setupProject_2(
			String			data_dir,
			String			argfile) {
		BundleUtils utils = new BundleUtils(SVCoreTestsPlugin.getDefault().getBundle());
		File project_dir = new File(fTmpDir, getName());
		
		assertTrue(project_dir.mkdirs());
		
		utils.copyBundleDirToFS(data_dir, project_dir);
		
		IProject p = TestUtils.createProject(getName(), project_dir);
		addProject(p);
		
		SVDBProjectManager pmgr = SVCorePlugin.getDefault().getProjMgr();
		SVDBProjectData pdata = pmgr.getProjectData(p);
		
		SVProjectFileWrapper fw = pdata.getProjectFileWrapper();
		fw.addArgFilePath(argfile);
		pdata.setProjectFileWrapper(fw);

		/*
		ISVDBIndexCache cache = fCacheFactory.createIndexCache(getName(), argfile);
		SVDBArgFileIndex2 index = new SVDBArgFileIndex2(getName(), 
				argfile, new SVDBWSFileSystemProvider(), cache, null);
	
		// Null builder so we control everything
		index.init(new NullProgressMonitor(), null);
	
		// Ensure the index is up-to-date
		index.loadIndex(new NullProgressMonitor());
		 */
		ISVDBIndex index = null;
		for (ISVDBIndex i : pdata.getProjectIndexMgr().getIndexList()) {
			if (i.getBaseLocation().equals(argfile)) {
				index = i;
				break;
			}
		}
		
		System.out.println("index=" + index);
		
		pmgr.rebuildProject(new NullProgressMonitor(), p);
		
		return new Tuple<ISVDBIndex, IProject>(index, p);
	}
	
}
