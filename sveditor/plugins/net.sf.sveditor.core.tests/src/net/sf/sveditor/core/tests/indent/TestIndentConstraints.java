package net.sf.sveditor.core.tests.indent;

import net.sf.sveditor.core.SVCorePlugin;
import net.sf.sveditor.core.tests.SVCoreTestCaseBase;

public class TestIndentConstraints extends SVCoreTestCaseBase {

	public void testIndentDistConstraint() {
		String doc = 
				"class someclass;\n" +
						"	constraint clock {\n" +
						"		clk_cfg.period dist {\n" +
						"			[1:10  ] :/ 1,\n" +
						"			11       := 1,\n" +
						"			12       := 1,\n" +
						"			[13: 15] :/ 1\n" +
						"		};\n" +
						"		clk_cfg.jitter < (3 * 1000);\n" +
						"	}\n" +
						"	function void my_func;\n" +
						"		my_class cls1; \n" +
						"		assert(cls1.randomize() with {\n" +
						"				cls1.a == 2;\n" +
						"			})\n" +
						"		else $display (\"ERROR\");\n" +
						"	endfunction\n" +
						"endclass\n"
						;				
		
		SVCorePlugin.getDefault().enableDebug(false);
		IndentTests.runTest(getName(), fLog, doc);
	}
	public void testIndentInsideConstraint() {
		String doc = 
			"class top;\n" +
			"	int a, b; \n" +
			"	constraint c_con {\n" +
			"		\n" +
			"		if(a == b) {\n" +
			"			a inside {1,3}; \n" +
			"		} \n" +
			"		else {\n" +
			"			a inside {0,2}; \n" +
			"		}\n" +
			"	}\n" +
			"endclass\n"
			;
		
		SVCorePlugin.getDefault().enableDebug(false);
		IndentTests.runTest(getName(), fLog, doc);
	}
	public void testIndentForeachConstraint() {
		String doc = 
			"class c extends uvm_sequence_item; //base input transaction\n" +
			"	constraint legal_values {\n" +
			"		// constraint datagram_commads \n" +
			"		foreach(datagram_commands[j]) \n" +
			"			{\n" +
			"			datagram_commands[j] <= 8'h14;\n" +
			"			if (datagram_commands[j]==DTGRAM_OP_NOP) {\n" +
			"				//datagram_payload[j] inside {0};   //the value or number of databytes is zero not the number of elements\n" +
			"				//datagram_bytelength_payload[j] == 0;\n" +
			"			}\n" +
			"			if (datagram_commands[j]==DTGRAM_OP_APRD) {\n" +
			"				//datagram_bytelength_payload[j] inside {[0:1486]};   \n" +
			"			}\n" +
			"		} //end datagram_commands constraint\n" +
		    "\n" +
			"	} //end legal values constraint\n" +
		    "\n" +
			"endclass : c\n"
		    ;
	
		
		SVCorePlugin.getDefault().enableDebug(false);
		IndentTests.runTest(getName(), fLog, doc);
	}
}
