using HeadsApi.Models;
using HeadsApi.Utils;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace HeadsApi.Controllers
{
    public class PointsController : ApiController
    {
        private HeadsEntities dbContext;

        public PointsController()
        {
            dbContext = new HeadsEntities();
        }

        // GET api/<controller>
        public IEnumerable<PointModel> Get()
        {
            return dbContext.Points.Take(200).ToList().Select(p =>
                 new PointModel{ Title = p.Title, 
                       Description = p.Description,
                       Latitude = p.Latitude,
                       Longitude = p.Longitude,
                       Uploaded = (long) p.Uploaded.Subtract(new DateTime(1970, 1, 1)).TotalMilliseconds,
                       PointId = p.PointId,
                       Username = p.AspNetUser.UserName});
        }

        // GET api/<controller>/5
        public Point Get(int id)
        {
            return dbContext.Points.Where(p => p.PointId == id).FirstOrDefault();
        }

        [Route("api/Points/search")]
        public IEnumerable<PointModel> Get(String username = null,
                                           double? latitude = null,
                                           double? longitude = null,
                                           double? range = null,
                                           long? from = null,
                                           long? to = null,
                                           String title = null,
                                           String description = null)
        {
            var points = dbContext.Points.Where(p => true);
            if (username != null)
            {
                points = points.Where(p => username.Equals(p.AspNetUser.UserName));
            }
            if (!String.IsNullOrWhiteSpace(title))
            {
                points = points.Where(p => p.Title.Contains(title));
            }
            if (!String.IsNullOrWhiteSpace(description))
            {
                points = points.Where(p => p.Description.Contains(description));
            }
            if (from != null && to != null)
            {
                DateTime fromDate = TimeUtils.UnixTimeStampToDateTime(from.Value);
                DateTime toDate = TimeUtils.UnixTimeStampToDateTime(to.Value);

                points = points.Where(p => p.Uploaded >= fromDate && p.Uploaded <= toDate);
            }
            if (latitude != null && longitude != null && range != null)
            {
                Position topLeft = new Position();
                Position bottomRight = new Position();

                LocationUtils.BoundingBox(latitude.Value, longitude.Value, 1000*range.Value,
                                          out topLeft, out bottomRight);

                Decimal bottomRightLatitude = new Decimal(bottomRight.Latitude);
                Decimal topLeftLatitude = new Decimal(topLeft.Latitude);
                Decimal topLeftLongitude = new Decimal(topLeft.Longitude);
                Decimal bottomRightLongitude = new Decimal(bottomRight.Longitude);

                points = points.Where(p => p.Latitude > bottomRightLatitude &&
                                           p.Latitude < topLeftLatitude &&
                                           p.Longitude > topLeftLongitude &&
                                           p.Longitude < bottomRightLongitude);
            }
            return points.Take(200).ToList().Select(p =>
                 new PointModel
                 {
                     Title = p.Title,
                     Description = p.Description,
                     Latitude = p.Latitude,
                     Longitude = p.Longitude,
                     Uploaded = (long)p.Uploaded.Subtract(new DateTime(1970, 1, 1)).TotalMilliseconds,
                     PointId = p.PointId,
                     Username = p.AspNetUser.UserName
                 });
        }

        // POST api/<controller>
        [Authorize]
        public int Post([FromBody]Point point)
        {
            point.Uploaded = DateTime.UtcNow;
            var username = User.Identity.Name;
            point.AspNetUser = dbContext.AspNetUsers.Where(u => u.UserName == username).FirstOrDefault();            
            dbContext.Points.Add(point);
            dbContext.SaveChanges();

            SavePhoto(point.PointId, point.Image);

            return point.PointId;
        }

        private void SavePhoto(int pointId, string encodedString)
        {
            var mappedPath = System.Web.Hosting.HostingEnvironment.MapPath("~/PhotosStorage");
            byte[] data = Convert.FromBase64String(encodedString);

            var filePath = string.Format("{0}\\photo{1}.jpg", mappedPath, pointId);
            var fileStream = new FileStream(filePath, FileMode.Create, FileAccess.ReadWrite);
            fileStream.Write(data, 0, data.Length);
            fileStream.Close();
        }

        // PUT api/<controller>/5
        [Authorize]
        public void Put(int id, [FromBody]Point point)
        {
            Point dbPoint = dbContext.Points.Where(p => p.PointId == id).FirstOrDefault();
            if (dbPoint != null)
            {
                dbPoint.Title = point.Title;
                dbPoint.Description = point.Description;
                dbPoint.Latitude = point.Latitude;
                dbPoint.Longitude = point.Longitude;
                dbContext.SaveChanges();
            }
        }

        // DELETE api/<controller>/5
        public void Delete(int id)
        {
            Point dbPoint = dbContext.Points.Where(p => p.PointId == id).FirstOrDefault();
            if (dbPoint != null)
            {
                dbContext.Points.Remove(dbPoint);
                dbContext.SaveChanges();
            }
        }
    }
}