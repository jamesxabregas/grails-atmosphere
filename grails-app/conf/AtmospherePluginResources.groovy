modules = {
    'atmosphere' {
        dependsOn 'jquery'
        resource id:'js', url:[plugin: 'atmosphere', dir:'js', file:"jquery.atmosphere.js"],
            disposition:'head', nominify: true
    }

}