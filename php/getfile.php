<?php
    require("./common.php");
    $data = htmlspecialchars($_GET["data"]);
    $hash = $data;
    $file = hashToPath($hash);
    if (file_exists($file)) {
        header('Content-Description: Modpack File Download' .$hash);
        header('Content-Type: application/octet-stream');
        header('Content-Disposition: attachment; filename='.basename($file));
        header('Content-Transfer-Encoding: binary');
        header('Expires: 0');
        header('Cache-Control: must-revalidate, post-check=0, pre-check=0');
        header('Pragma: public');
        header('Content-Length: ' . filesize($file));
        header('Content-MD5: '.basename($file));
        
        readfile($file);
        
    } else {
        echo "<h1>Content Error!</h1><p>The file does not exist!</p>";
    }
?>