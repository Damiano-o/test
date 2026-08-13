$out_dir = 'out';
$pdf_mode = 1;
END { system('cmd /c copy /Y out\*.pdf . >nul 2>&1') if defined $out_dir; }