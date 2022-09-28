.text
.globl  asm_main
asm_main:
pushq   %rbp
movq    %rsp,%rbp
movq	$20,%rax
pushq	%rax
movq	$50,%rax
popq	%rdx
subq	%rdx,%rax
negq	%rax
movq    %rax,%rdi
call    put
movq    %rbp,%rsp
popq    %rbp
ret
