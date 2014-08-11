<?php
    require('./common.php');
    $data = $_GET["data"];
    $json = json_decode($data, true);
    $size = 0;
    foreach ($json as $value){
        $file = hashToPath($value);
        if (file_exists($file)){
            $size = $size + filesize($file);
        }else{
            echo "INVALID FILE: " . $file . "\n";
        }
    }
    echo $size;
?>