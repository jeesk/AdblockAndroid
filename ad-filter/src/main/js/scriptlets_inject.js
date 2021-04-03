(function () {
    function run() {
        if (document.sls{{HIDDEN_FLAG}} === true) {
            return;
        }
        var scriptletArray = JSON.parse({{BRIDGE}}.getScriptlets(document.location.href));
        for (let item of scriptletArray) {
            let script = scriptlets.invoke({
                name: item[0],
                args: item.slice(1)
            });
            {{DEBUG}} !script && console.log(`invalid scriptlets: ${JSON.stringify(item)}`);
            try {
                // don't use eval() here, it may be blocked by scriptlets
                new Function(script)();
            } catch (err) {
                {{DEBUG}} console.log('scriptlets went wrong: ' + err);
            }
        }
        {{DEBUG}} console.log(`applied ${scriptletArray.length} scriptlets for ${document.location.href}`);
        document.sls{{HIDDEN_FLAG}} = true;
    }

    if (document.readyState === 'complete') {
        run();
    } else {

        document.onreadystatechange = function () {
            if (document.readyState == 'interactive') {
                run();
            }
        }

        window.addEventListener('load', run);

        document.addEventListener('DOMContentLoaded', run, false);
    }
})();