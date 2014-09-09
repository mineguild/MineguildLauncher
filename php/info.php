<?php
    require('./common.php');
    $data = $_POST["data"];
    $decoded = base64_decode($data);
    $json = json_decode($decoded, true);
    $size = 0;
    
    foreach ($json as $value){
        $file = hashToPath($value);
        if (file_exists($file)){
            $size = $size + filesize($file);
        }else{
            echo "INVALID FILE: " . $file . "\n";
        }
    }
    $line = date('Y-m-d H:i:s') . " - $_SERVER[REMOTE_ADDR] - Download size request - ". $size/1024 . " kb";
    file_put_contents('ip.log', $line . PHP_EOL, FILE_APPEND);
    echo $size;
?>