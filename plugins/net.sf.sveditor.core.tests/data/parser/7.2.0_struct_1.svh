
class c;
	
struct { bit [7:0] opcode; bit [23:0] addr; } IR; // anonymous structure

// defines variable IR
function f;
	IR.opcode = 1; // set field in IR.
endfunction

/*
typedef struct {
	bit [7:0] opcode;
	bit [23:0] addr;
} instruction; // named structure type

instruction IR; // define variable
 */

endclass