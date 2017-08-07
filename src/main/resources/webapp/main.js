var app=angular.module('app', ['ngFileUpload']);

app.controller('controller', function($scope, Upload) {
    $scope.send = function() {
        $scope.errorMessage = null;
        if (!$scope.catalogueCsvFile) {
            $scope.errorMessage = "Válassz ki egy CSV fájlt!"
            return;
        }
        if (!$scope.catalogueTitle) {
            $scope.errorMessage = "Írj be egy katalógus címet!"
            return;
        }
        Upload.upload({
            url: '/service/csvRendering',
            data: {
                'file': $scope.catalogueCsvFile,
                'title': $scope.catalogueTitle,
                'imageIncluded': !$scope.imageIncluded ? false : true
            }
        }).then(function (resp) {
            console.log('Success ' + resp.config.data.file.name + 'uploaded. Response: ' + resp.data.requestId);
        }, function (resp) {
            $scope.errorMessage = "A feltöltés nem sikerült. Próbáld újra..."
        });
    }

});
