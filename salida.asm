; Compilador de Pseudocodigo a Ensamblador x86
; Generado automaticamente

.386
.model flat, stdcall
option casemap:none

include \masm32\include\windows.inc
include \masm32\include\kernel32.inc
include \masm32\include\masm32.inc
include \masm32\include\msvcrt.inc

includelib \masm32\lib\kernel32.lib
includelib \masm32\lib\masm32.lib
includelib \masm32\lib\msvcrt.lib

.data
    formato_out DB "%d", 10, 0
    formato_in DB "%d", 0
    buffer DB 100 DUP(??)
    x DWORD ?
    y DWORD ?
    suma DWORD ?
    contador DWORD ?

.code
start:
    invoke crt_scanf, ADDR formato_in, ADDR x
    invoke crt_scanf, ADDR formato_in, ADDR y
    mov eax, x
    push eax
    mov eax, y
    pop ebx
    add eax, ebx
    mov suma, eax
    mov eax, suma
    invoke crt_printf, ADDR formato_out, eax
    mov eax, suma
    push eax
    mov eax, 10
    pop ebx
    cmp ebx, eax
    jle L0
    mov eax, suma
    invoke crt_printf, ADDR formato_out, eax
L0:
    mov eax, 1
    mov contador, eax
L1:
    mov eax, contador
    push eax
    mov eax, 5
    pop ebx
    cmp ebx, eax
    jge L2
    mov eax, contador
    push eax
    mov eax, 1
    pop ebx
    add eax, ebx
    mov contador, eax
    mov eax, contador
    invoke crt_printf, ADDR formato_out, eax
    jmp L1
L2:

    invoke ExitProcess, 0
end start
