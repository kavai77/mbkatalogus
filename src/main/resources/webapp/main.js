var app=angular.module('app', ['ngFileUpload', 'ui.bootstrap']);

app.controller('controller', function($scope, Upload, $interval, $http) {
    $scope.send = function() {
        $scope.errorMessage = null;
        $scope.requestId = null;
        $scope.errorCount = 0;
        $scope.warningCount = 0;
        $scope.errorItems = new Array();
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
                'csvFile': $scope.catalogueCsvFile,
                'title': $scope.catalogueTitle,
                'quality': $scope.quality,
                'wholeSaleFormat': $scope.wholeSaleFormat,
                'autoLineBreakAfterMinQty': $scope.autoLineBreakAfterMinQty,
                'headerImage': $scope.headerImage,
                'wideHeaderImage': $scope.wideHeaderImage,
                'footerImage': $scope.footerImage,
                'wideFooterImage': $scope.wideFooterImage,
            }
        }).then(function (resp) {
            $scope.requestId = resp.data.requestId;
            $scope.totalPageCount = 0;
            $scope.currentPageNumber = 0;
            $scope.totalLogoImageSize = "0 MB";
            $scope.totalImageSize = "0 MB";
            stop = $interval(function() {
                $http.get('/service/pollUserInfo?requestId=' + $scope.requestId).then(
                function successCallback(response){
                    $scope.generatedDocuments = response.data.generatedDocuments;
                    $scope.totalPageCount = response.data.totalPageCount;
                    $scope.currentPageNumber = response.data.currentPageNumber;
                    $scope.appendErrorItems(response.data.errorItems);
                    $scope.done = response.data.done;
                    $scope.totalLogoImageSize = response.data.totalLogoImageSize;
                    $scope.totalImageSize = response.data.totalImageSize;
                    if ($scope.done) {
                        $interval.cancel(stop);
                    }
                }, function errorCallback(response) {
                    console.log(JSON.stringify(response));
                });
            }, 1000)
        }, function (resp) {
            $scope.errorMessage = "A feltöltés nem sikerült. Próbáld újra..."
        });
    }

    $scope.cancel = function() {
        $http.get('/service/cancel?requestId=' + $scope.requestId);
    }

    $http.get('/service/indexbootstrap').then(
        function successCallback(response){
            $scope.pageTitle = response.data.pageTitle;
            $scope.catalogueTitle = response.data.lastDocumentTitle;
            $scope.quality = response.data.lastQuality != null ? response.data.lastQuality : "DRAFT";
            $scope.wholeSaleFormat = response.data.lastWholeSaleFormat != null ? response.data.lastWholeSaleFormat : "true";
            $scope.autoLineBreakAfterMinQty = response.data.lastAutoLineBreakAfterMinQty;
            $scope.wideHeaderImage = response.data.lastLastWideHeaderImage;
            $scope.wideFooterImage = response.data.lastLastWideFooterImage;
            $scope.productGroupsWithoutChapter = response.data.productGroupsWithoutChapter;
            $scope.version = response.data.buildTimestamp;
        }, function errorCallback(response) {
            $scope.quality = "DRAFT";
            $scope.wholeSaleFormat = "true";
            $scope.autoLineBreakAfterMinQty = false;
            scope.wideHeaderImage = false;
            scope.wideFooterImage = false;
            console.log(JSON.stringify(response));
        }
    );

    $scope.errorStyle = new Object();
    $scope.errorStyle["INFO"] = "alert alert-success";
    $scope.errorStyle["WARN"] = "alert alert-warning";
    $scope.errorStyle["ERROR"] = "alert alert-danger";
    $scope.hiddenErrorItems = new Object();

    $scope.appendErrorItems = function(errorItems) {
        for (i = 0; i < errorItems.length;i++) {
            switch (errorItems[i].severity) {
                case "WARN":
                    $scope.warningCount++;
                    break;
                case "ERROR":
                    $scope.errorCount++;
            }
            $scope.errorItems.push(errorItems[i]);
        }
    }

    $scope.addProductGroup = function() {
        var productGroup = prompt("Cikkcsoport neve");
        if (productGroup != null) {
            $scope.productGroupsWithoutChapter.push(productGroup);
        }
    }

    $scope.removeProductGroup = function(productGroup) {
        var index = $scope.productGroupsWithoutChapter.indexOf(productGroup);
        if (index > -1) {
            $scope.productGroupsWithoutChapter.splice(index, 1);
        }
    }

    $scope.saveProductGroups = function() {
        $http.post('/service/saveproductgroupwithoutchapter', $scope.productGroupsWithoutChapter).then(
            function successCallback(response){
                $('#productGroupsWithoutChapterModal').modal('hide');
            }, function errorCallback(response) {
                alert("Hiba történt mentés során");
                console.log(JSON.stringify(response));
            }
        );
    }

    $scope.reloadProductGroups = function() {
        $('#productGroupsWithoutChapterModal').modal('hide');
        $http.get('/service/indexbootstrap').then(
            function successCallback(response) {
                $scope.productGroupsWithoutChapter = response.data.productGroupsWithoutChapter;
            }
        );
    }
});
