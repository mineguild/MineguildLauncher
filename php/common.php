<?php
    $FILE_BASEPATH = "/var/www/mineguild.de/download/mmp/files/";
    function hashToPath($hash) {
        global $FILE_BASEPATH;
        $dir = $FILE_BASEPATH . substr($hash, 0, 2);
        $file = $dir . "/" . $hash;
        return $file;
    }
?>