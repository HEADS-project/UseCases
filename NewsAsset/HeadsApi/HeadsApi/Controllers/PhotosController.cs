using HeadsApi.Utils;
using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace HeadsApi.Controllers
{
    public class PhotosController : Controller
    {
        //
        // GET: /Photos/Full/{id}
        public ActionResult Full(int id)
        {
            var mappedPath = System.Web.Hosting.HostingEnvironment.MapPath("~/PhotosStorage");
            var filePath = string.Format("{0}\\photo{1}.jpg", mappedPath, id);

            if (!System.IO.File.Exists(filePath))
            {
                throw new HttpException(404, "Not found");
            }

            return File(filePath, "image/jpg");
        }

        //
        // GET: /Photos/Thumbnail/{id}
        public ActionResult Thumbnail(int id)
        {
            var thumbnailStoragePath = 
                System.Web.Hosting.HostingEnvironment.MapPath("~/ThumbnailsStorage");
            var thumbnailFilePath = string.Format("{0}\\photo{1}.jpg", thumbnailStoragePath, id);

            if (System.IO.File.Exists(thumbnailFilePath))
            {
                return File(thumbnailFilePath, "image/jpg");    
            }

            var mappedPath = System.Web.Hosting.HostingEnvironment.MapPath("~/PhotosStorage");
            var filePath = string.Format("{0}\\photo{1}.jpg", mappedPath, id);

            if (!System.IO.File.Exists(filePath))
            {
                throw new HttpException(404, "Not found");
            }

            Image image = new Bitmap(filePath);
            Image thumb = ImageUtilities.ResizeImage(image, 100);
            thumb.Save(thumbnailFilePath, ImageFormat.Jpeg);
            image.Dispose();
            thumb.Dispose();

            return File(thumbnailFilePath, "image/jpg");    
        }
	}
}