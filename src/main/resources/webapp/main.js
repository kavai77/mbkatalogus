var app=angular.module('app', ['ngFileUpload']);

app.controller('controller', function($scope, Upload, $interval, $http) {
    $scope.send = function() {
        $scope.errorMessage = null;
        $scope.requestId = null;
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
            $scope.requestId = resp.data.requestId;
            $interval(function() {
                $http.get('/service/pollUserInfo?requestId=' + $scope.requestId).then(
                function successCallback(response){
                    $('#errorsPlaceholder').append(JSON.stringify(response.data) + "<br>");
                }, function errorCallback(response) {
                    console.log(JSON.stringify(response));
                });
            }, 1000)
        }, function (resp) {
            $scope.errorMessage = "A feltöltés nem sikerült. Próbáld újra..."
        });
    }

});
