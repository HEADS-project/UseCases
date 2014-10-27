using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace HeadsApi.Utils
{
    /// <summary>
    /// The distance type to return the results in.
    /// </summary>
    public enum DistanceType { Miles, Meters };

    /// <summary>
    /// Specifies a Latitude / Longitude point.
    /// </summary>
    public struct Position
    {
        public double Latitude;
        public double Longitude;
    }

    public static class LocationUtils
    {
        /// <summary>
        /// Returns the distance in miles or meters of any two
        /// latitude / longitude points.
        /// </summary>
        public static double Distance(Position pos1, Position pos2, DistanceType type)
        {
            double R = (type == DistanceType.Miles) ? 3960000 : 6371000;

            double dLat = DegreeToRadian(pos2.Latitude - pos1.Latitude);
            double dLon = DegreeToRadian(pos2.Longitude - pos1.Longitude);

            double a = Math.Sin(dLat / 2) * Math.Sin(dLat / 2) +
                Math.Cos(DegreeToRadian(pos1.Latitude)) * Math.Cos(DegreeToRadian(pos2.Latitude)) *
                Math.Sin(dLon / 2) * Math.Sin(dLon / 2);
            double c = 2 * Math.Asin(Math.Min(1, Math.Sqrt(a)));
            double d = R * c;

            return d;
        }

        /// <summary>
        /// Given a latitude/longitude and a distance the bottom right and top left corners of the bounding box are returned
        /// </summary>
        /// <param name="latitude"></param>
        /// <param name="longitude"></param>
        /// <param name="distance"></param>
        /// <param name="topLeft"></param>
        /// <param name="bottomRight"></param>
        public static void BoundingBox(double latitude, double longitude, double distance, out Position topLeft, out Position bottomRight)
        {
            // 1 meter = 0.000621371192 miles
            distance *= 0.000621371192;

            // 1° of latitude ~= 69 miles
            // 1° of longitude ~= cos(latitude)*69
            double lon1 = longitude - distance / Math.Abs(Math.Cos(DegreeToRadian(latitude)) * 69);
            double lon2 = longitude + distance / Math.Abs(Math.Cos(DegreeToRadian(latitude)) * 69);
            double lat1 = latitude - (distance / 69);
            double lat2 = latitude + (distance / 69);

            topLeft.Latitude = lat2;
            topLeft.Longitude = lon1;

            bottomRight.Latitude = lat1;
            bottomRight.Longitude = lon2;
        }

        private static double DegreeToRadian(double angle)
        {
            return (Math.PI * angle) / 180.0;
        }

        private static double RadianToDegree(double angle)
        {
            return (angle * 180.0) / Math.PI;
        }
    }

}