<?php 
    require('database.php');
    $UUID = $_GET["uuid"];
    if (!($stmt = $mysqli->prepare("SELECT AccountLevel FROM users WHERE UUID=?"))) {
        echo "Prepare failed: (" . $mysqli->errno . ") " . $mysqli->error;
    }
    if (!$stmt->bind_param("s", $UUID)) {
        echo "Binding parameters failed: (" . $stmt->errno . ") " . $stmt->error;
    }
    if (!$stmt->execute()) {
        echo "Execute failed: (" . $stmt->errno . ") " . $stmt->error;
    }
    
    $out_account = NULL;
    if (!$stmt->bind_result($out_account)) {
        echo "Binding output parameters failed: (" . $stmt->errno . ") " . $stmt->error;
    }
    while ($stmt->fetch()) {
        //printf("id = %s (%s)\n", $out_account, gettype($out_account));
        printf("Lvl=%s\n", $out_account);
    }
    printf("END");
?>
    