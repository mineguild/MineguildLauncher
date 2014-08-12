<?php
    require('common.php');
    $json = json_decode($_GET["data"], true);
    $needed = array();
    foreach($json as $hash){
        $file = hashToPath($hash);
        if(!file_exists($file)){
            $needed[] = $hash;
        }
    }
    echo json_encode($needed);
?>